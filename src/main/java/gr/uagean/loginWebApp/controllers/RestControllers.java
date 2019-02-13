/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.eidas.sp.SpAuthenticationRequestData;
import eu.eidas.sp.SpAuthenticationResponseData;
import eu.eidas.sp.SpEidasSamlTools;
import eu.eidas.sp.metadata.GenerateMetadataAction;
import gr.uagean.loginWebApp.MemCacheConfig;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.factory.AttributeSetFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.model.pojo.UpdateDataRequest;
import gr.uagean.loginWebApp.service.EidasPropertiesService;
import gr.uagean.loginWebApp.service.EsmoMetadataService;
import gr.uagean.loginWebApp.service.ParameterService;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;
import gr.uagean.loginWebApp.service.NetworkServiceOld;

/**
 *
 * @author nikos
 */
@Controller
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST})
public class RestControllers {

    @Autowired
    private EidasPropertiesService propServ;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ParameterService paramServ;

    @Autowired
    private NetworkServiceOld netServ;

    @Autowired
    private EsmoMetadataService metadataServ;

    @Value("${eidas.error.consent}")
    private String EIDAS_CONSENT_ERROR;
    @Value("${eidas.error.qaa}")
    private String EIDAS_QAA_ERROR;
    @Value("${eidas.error.missing}")
    private String EIDAS_MISSING_ATTRIBUTE_ERROR;
    @Value("${eidas.error.consent.gr}")

    private final static Logger LOG = LoggerFactory.getLogger(RestControllers.class);

    private final static String SP_COUNTRY = "SP_COUNTRY";
    private final static String SP_FAIL_PAGE = "SP_FAIL_PAGE";

    final static String UAEGEAN_LOGIN = "UAEGEAN_LOGIN";
    final static String LINKED_IN_SECRET = "LINKED_IN_SECRET";

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    @RequestMapping(value = "/metadata", method = {RequestMethod.POST, RequestMethod.GET}, produces = {"application/xml"})
    public @ResponseBody
    String metadata() {
        GenerateMetadataAction metaData = new GenerateMetadataAction();
        return metaData.generateMetadata().trim();
    }

    @RequestMapping(value = "/generateSAMLToken", method = {RequestMethod.GET})
    public ResponseEntity getSAMLToken(@RequestParam(value = "citizenCountry", required = true) String citizenCountry, @RequestParam(value = "IdPMSSession", required = true) String session) {

        String serviceProviderCountry = paramServ.getParam(SP_COUNTRY);
        try {
            ArrayList<String> pal = new ArrayList();
            pal.addAll(propServ.getEidasProperties());
            SpAuthenticationRequestData data
                    = SpEidasSamlTools.generateEIDASRequest(pal, citizenCountry, serviceProviderCountry);
            // get and store external session Id to the SM ms
            Cache memcache = this.cacheManager.getCache(MemCacheConfig.IDPMS_SESSION);
            if (memcache == null) {
                throw new NullPointerException("error getting ipd cache");
            } else {
                String esmoGWSession = memcache.get(session, String.class);
                String eidasSession = data.getID();

                String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
//                List<NameValuePair> postParams = new ArrayList();
//                postParams.add(new NameValuePair("sessionId", esmoGWSession));
//                postParams.add(new NameValuePair("dataObject", eidasSession));
//                postParams.add(new NameValuePair("variableName", "GR_eIDAS_IdP_Session"));
                UpdateDataRequest updateReq = new UpdateDataRequest(esmoGWSession, "GR_eIDAS_IdP_Session", eidasSession);
                SessionMngrResponse resp = netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json");
                if (resp.getCode().toString().equals("OK")) {
                    // IdP Connector generates “external session identifier” for the authentication (e.g. eIDAS uuid) and stores it in the internal session by calling post, “/sm/updateSessionData”
                    return ResponseEntity.ok(data.getSaml());
                }
            }
        } catch (NullPointerException e) {
            LOG.error("NulPointer Caught", e);
        } catch (IOException ex) {
            LOG.error("io Caught", ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("NoSuchAlgorithmException Caught", ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @RequestMapping(value = "/eidasResponse", method = {RequestMethod.POST, RequestMethod.GET})
    public String eidasResponse(@RequestParam(value = "SAMLResponse", required = false) String samlResponse,
            HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttrs, @CookieValue(value = "localeInfo", required = false) String langCookie, Model model) {

        LOG.debug("The node responeded with;");
        LOG.debug(samlResponse);
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();
        SpAuthenticationResponseData data = SpEidasSamlTools.processResponse(samlResponse, remoteAddress);
        String redirectToError = checkEidasResponseForErrorCode(data.getResponseXML(), redirectAttrs);
        if (redirectToError != null) {
            return redirectToError;
        }

        try {
            String eidasSession = data.getID();
            //receives response containing the external identifier and retrieves the internal identifier by calling, get “/sm/getSession”
            String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
            List<NameValuePair> requestParams = new ArrayList();
            requestParams.add(new NameValuePair("varName", "GR_eIDAS_IdP_Session"));
            requestParams.add(new NameValuePair("varValue", eidasSession));
            SessionMngrResponse resp = netServ.sendGet(sessionMngrUrl, "/sm/getSession", requestParams);
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                return "redirect:" + paramServ.getParam(SP_FAIL_PAGE);
            }

            //IdP Connector updates the session with the variables received from the user authentication by calling the SM, with post, “/sm/updateSessionData” twice
            //once to store the received attributes
            ObjectMapper mapper = new ObjectMapper();
            AttributeSet receivedAttributes = AttributeSetFactory.makeFromEidasResponse("id", TypeEnum.Response, "issuer", "recipient", data.getResponseXML());
            String attributSetString = mapper.writeValueAsString(receivedAttributes);
            requestParams.clear();
            requestParams.add(new NameValuePair("dataObject", attributSetString));
            requestParams.add(new NameValuePair("variableName", "dsResponse"));
            requestParams.add(new NameValuePair("sessionId", resp.getSessionData().getSessionId()));
            UpdateDataRequest updateReq = new UpdateDataRequest(resp.getSessionData().getSessionId(), "dsResponse", attributSetString);
            resp = netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json");
//            resp = netServ.sendPost(sessionMngrUrl, "/sm/updateSessionData", requestParams);
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                return "redirect:" + paramServ.getParam(SP_FAIL_PAGE);
            }

            // once to store the idp metadata
            requestParams.clear();
//            requestParams.add(new NameValuePair("dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata())));
//            requestParams.add(new NameValuePair("sessionId", resp.getSessionData().getSessionId()));
//            resp = netServ.sendPost(sessionMngrUrl, "/sm/updateSessionData", requestParams);
            updateReq = new UpdateDataRequest(resp.getSessionData().getSessionId(), "dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata()));
            resp = netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json");
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                return "redirect:" + paramServ.getParam(SP_FAIL_PAGE);
            }
            //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken” 
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", resp.getSessionData().getSessionId()));
            resp = netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams);
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                return "redirect:" + paramServ.getParam(SP_FAIL_PAGE);
            } else {
                String msToken = resp.getAdditionalData();
                //IdP calls, post  /acm/response
                String acmUrl = paramServ.getParam("ACM_URL");
                model.addAttribute("msToken", msToken);
                model.addAttribute("acmUrl", acmUrl + "/acm/response");
                return "acmRedirect";
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException e) {
            LOG.debug(e.getMessage());
            return "redirect:" + paramServ.getParam(SP_FAIL_PAGE);
        }
    }

    @RequestMapping(value = "/idp/authenticate", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(@RequestParam(value = "msToken", required = true) String token, HttpServletRequest request) {

        request.setAttribute(
                View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.FOUND);

        ObjectMapper mapper = new ObjectMapper();

        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        List<NameValuePair> getParams = new ArrayList();
        getParams.add(new NameValuePair("token", token));

        try {
            //calls SM, get /sm/validateToken, to validate the received token and get the sessionId
            SessionMngrResponse resp = netServ.sendGet(sessionMngrUrl, "/sm/validateToken", getParams);
            if (resp.getCode().toString().equals("OK") && StringUtils.isEmpty(resp.getError())) {
                String sessionId = resp.getSessionData().getSessionId();
                getParams.clear();
                getParams.add(new NameValuePair("sessionId", sessionId));

                //cache the sesionId to retrieve it after front channel redirection
                String idpMsSessionId = UUID.randomUUID().toString();
                Cache memCache = this.cacheManager.getCache(MemCacheConfig.IDPMS_SESSION);
                if (memCache == null) {
                    throw new NullPointerException("could not get idp session cache");
                } else {
                    memCache.put(idpMsSessionId, sessionId);
                }

                //calls SM, “/sm/getSessionData” to get the session object that must contain the variables idpRequest, idpMetadata 
                resp = netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", getParams);
                String idpRequest = (String) resp.getSessionData().getSessionVariables().get("idpRequest");
                //TODO check the IDP metdata?
                String idpMetadata = (String) resp.getSessionData().getSessionVariables().get("idpMetadata");

                AttributeSet idpRequestObject = mapper.readValue(idpRequest, AttributeSet.class);
                final Set<String> VALUES = new HashSet<>(Arrays.asList(
                        paramServ.getParam("EIDAS_PROPERTIES").trim().split(",")
                ));

                List<AttributeType> supportedAttrs = Arrays.stream(idpRequestObject.getAttributes()).filter(attribute -> {
                    return VALUES.contains(attribute.getFriendlyName());
                }).collect(Collectors.toList());

                if (supportedAttrs.isEmpty()) {
                    LOG.error("none of the attributes requested are supported!");
                    return new ModelAndView("redirect:/authfail");
                }

                return new ModelAndView("redirect:/login?session=" + idpMsSessionId);
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.error("error sending getRequest");
            LOG.error(e.getMessage());

        } catch (IOException e) {
            LOG.error("error marshaling idpRequest from session");
            LOG.error(e.getMessage());

        } catch (NullPointerException e) {
            LOG.error("error getting external session from cache");
            LOG.error(e.getMessage());
        }
        //then redirect to the front-end user authentication UI
        return new ModelAndView("redirect:/authfail");
    }

    private String checkEidasResponseForErrorCode(String eidasResponse, RedirectAttributes redirectAttrs) {
        if (eidasResponse.contains("202007") || eidasResponse.contains("202004")
                || eidasResponse.contains("202012") || eidasResponse.contains("202010")
                || eidasResponse.contains("003002")) {

            if (eidasResponse.contains("202007") || eidasResponse.contains("202012")) {
                LOG.debug("---------------202012!!!!!!!");
                redirectAttrs.addFlashAttribute("title", "Registration/Login Cancelled");
                redirectAttrs.addFlashAttribute("errorMsg", EIDAS_CONSENT_ERROR);
                return "redirect:" + System.getenv(URL_PREFIX) + "/authfail?reason=consent";

            }
            if (eidasResponse.contains("202004")) {
                redirectAttrs.addFlashAttribute("title", "Registration/Login Cancelled");
                redirectAttrs.addFlashAttribute("errorMsg", EIDAS_QAA_ERROR);
                return "redirect:" + System.getenv(URL_PREFIX) + "/authfail?reason=qa";

            }
            if (eidasResponse.contains("202010")) {
                redirectAttrs.addFlashAttribute("title", "Registration/Login Cancelled");
                redirectAttrs.addFlashAttribute("errorMsg", EIDAS_MISSING_ATTRIBUTE_ERROR);
                return "redirect:" + System.getenv(URL_PREFIX) + "/authfail?rearon=attr";

            }
            if (eidasResponse.contains("003002")) {
                redirectAttrs.addFlashAttribute("title", "Non-sucessful authentication");
                redirectAttrs.addFlashAttribute("errorMsg", "Please, return to the home page and re-initialize the process. If the authentication fails again, please contact your national eID provider");
                return "redirect:" + System.getenv(URL_PREFIX) + "/authfail?reason=fail";
            }
        }
        return null;
    }

}
