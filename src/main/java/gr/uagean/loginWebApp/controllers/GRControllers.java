/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.eidas.sp.ApplicationSpecificServiceException;
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
import gr.uagean.loginWebApp.service.HttpSignatureService;
import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.MSConfigurationService;
import gr.uagean.loginWebApp.service.NetworkService;
import gr.uagean.loginWebApp.service.ParameterService;
import gr.uagean.loginWebApp.service.impl.HttpSignatureServiceImpl;
import gr.uagean.loginWebApp.service.impl.NetworkServiceImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
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

/**
 *
 * @author nikos
 */
@Controller
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST})
public class GRControllers {

    @Autowired
    private EidasPropertiesService propServ;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ParameterService paramServ;

    private final NetworkService netServ;
    private final KeyStoreService keyServ;

    @Autowired
    private EsmoMetadataService metadataServ;

    @Autowired
    private MSConfigurationService configServ;

    @Value("${eidas.error.consent}")
    private String EIDAS_CONSENT_ERROR;
    @Value("${eidas.error.qaa}")
    private String EIDAS_QAA_ERROR;
    @Value("${eidas.error.missing}")
    private String EIDAS_MISSING_ATTRIBUTE_ERROR;
    @Value("${eidas.error.consent.gr}")

    private final static Logger LOG = LoggerFactory.getLogger(GRControllers.class);

    private final static String SP_COUNTRY = "SP_COUNTRY";
    private final static String SP_FAIL_PAGE = "SP_FAIL_PAGE";

    final static String UAEGEAN_LOGIN = "UAEGEAN_LOGIN";
    final static String LINKED_IN_SECRET = "LINKED_IN_SECRET";

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    @Autowired
    public GRControllers(KeyStoreService keyServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getHttpSigningKey();
        String fingerPrint = DigestUtils.sha256Hex(keyServ.getHttpSigPublicKey().getEncoded());
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(this.keyServ);
    }

    @RequestMapping(value = "/metadata", method = {RequestMethod.POST, RequestMethod.GET}, produces = {"application/xml"})
    public @ResponseBody
    String metadata() {
        GenerateMetadataAction metaData = new GenerateMetadataAction();
        return metaData.generateMetadata().trim();
    }

    @RequestMapping(value = "/eidasResponse", method = {RequestMethod.POST, RequestMethod.GET})
    public String eidasResponse(@RequestParam(value = "SAMLResponse", required = false) String samlResponse,
            HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttrs, @CookieValue(value = "localeInfo", required = false) String langCookie, Model model) {

        ObjectMapper mapper = new ObjectMapper();

        LOG.debug("The node responeded with;");
        LOG.debug(samlResponse);
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        try {

            SpAuthenticationResponseData data = SpEidasSamlTools.processResponse(samlResponse, remoteAddress);
            String redirectToError = checkEidasResponseForErrorCode(data.getResponseXML(), redirectAttrs);
            if (redirectToError != null) {
                return redirectToError;
            }

            String eidasSession = data.getResponseToID();//data.getID();

            //receives response containing the external identifier and retrieves the internal identifier by calling, get “/sm/getSession”
            String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
            List<NameValuePair> requestParams = new ArrayList();
            requestParams.add(new NameValuePair("varName", "GR_eIDAS_IdP_Session"));
            requestParams.add(new NameValuePair("varValue", eidasSession));
            SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSession", requestParams, 1), SessionMngrResponse.class);
            LOG.debug("tried to retrieve session for " + eidasSession);
            LOG.debug("getSession " + resp.getCode().toString());
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                redirectAttrs.addFlashAttribute("errorMsg", "error communicating with ESMO GW");
                return "redirect:/eidas/authfail?reason=esmo";
            }

            String smSessionId = resp.getSessionData().getSessionId();
            redirectAttrs.addFlashAttribute("sessionId", smSessionId);

            //IdP Connector updates the session with the variables received from the user authentication by calling the SM, with post, “/sm/updateSessionData” twice
            //once to store the received attributes
            String id = UUID.randomUUID().toString();
            AttributeSet receivedAttributes = AttributeSetFactory.makeFromEidasResponse(id, TypeEnum.AUTHRESPONSE, "issuer", "recipient", data.getResponseXML());
            String attributSetString = mapper.writeValueAsString(receivedAttributes);
            requestParams.clear();
            requestParams.add(new NameValuePair("dataObject", attributSetString));
            requestParams.add(new NameValuePair("variableName", "dsResponse"));
            requestParams.add(new NameValuePair("sessionId", smSessionId));
            UpdateDataRequest updateReq = new UpdateDataRequest(smSessionId, "dsResponse", attributSetString);
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
            LOG.debug("updateSessionData " + resp.getCode().toString());
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                redirectAttrs.addFlashAttribute("errorMsg", "error communicating with ESMO GW");
                return "redirect:/eidas/authfail?reason=esmo";
            }

            // once to store the idp metadata
            requestParams.clear();
            updateReq = new UpdateDataRequest(smSessionId, "dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata()));
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
            LOG.info("updateSessionData metadata " + resp.getCode().toString());
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                redirectAttrs.addFlashAttribute("errorMsg", "error communicating with ESMO GW");
                return "redirect:/eidas/authfail?reason=esmo";
            }
            //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken”
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", smSessionId));
            requestParams.add(new NameValuePair("sender", paramServ.getParam("REDIRECT_JWT_SENDER"))); //[TODO] add correct sender "IdPms001"
            requestParams.add(new NameValuePair("receiver", paramServ.getParam("REDIRECT_JWT_RECEIVER"))); //"ACMms001"
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams, 1), SessionMngrResponse.class);
            LOG.info("generateToken" + resp.getCode().toString());
            if (!resp.getCode().toString().equals("NEW")) {
                LOG.error("ERROR: " + resp.getError());
                redirectAttrs.addAttribute("errorMsg", "error communicating with ESMO GW");
                return "redirect:/eidas/authfail?reason=esmo";
            } else {
                String msToken = resp.getAdditionalData();
                //IdP calls, post  /acm/response
//                String acmUrl = paramServ.getParam("ACM_URL");
                model.addAttribute("msToken", msToken);
//                model.addAttribute("acmUrl", acmUrl + "/acm/response");
                model.addAttribute("acmUrl", configServ.getMsEndpointByIdAndApiCall(paramServ.getParam("ACM_ID"), "acmResponse"));
                return "acmRedirect";
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException e) {
            LOG.debug(e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Internal Server Error");
            return "redirect:/eidas/authfail?reason=esmo";
        } catch (ApplicationSpecificServiceException e) {
            LOG.debug(e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Error reading eIDAS Assertions, assertions cannot be null");
            return "redirect:/eidas/authfail?reason=esmo";
        }
    }

    @RequestMapping(value = "/gr/idp/authenticate", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(HttpServletRequest request,
            RedirectAttributes redirectAttrs, Model model,
            @RequestParam(value = "sessionId", required = false) String esmoSessionId
    ) {

        request.setAttribute(
                View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.FOUND);

        ObjectMapper mapper = new ObjectMapper();

        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        List<NameValuePair> getParams = new ArrayList();
//        getParams.add(new NameValuePair("token", token));
        SessionMngrResponse resp;

        try {
            //get session from redirect attribuets
//            String esmoSessionId = (String) model.asMap().get("esmoSessionId");
            getParams.clear();
            getParams.add(new NameValuePair("sessionId", esmoSessionId));

            //cache the sesionId to retrieve it after front channel redirection
            String idpMsSessionId = DigestUtils.sha256Hex(UUID.randomUUID().toString());
            Cache memCache = this.cacheManager.getCache(MemCacheConfig.IDPMS_SESSION);
            if (memCache == null) {
                throw new NullPointerException("could not get idp session cache");
            } else {
                memCache.put(idpMsSessionId, esmoSessionId);
            }

            //calls SM, “/sm/getSessionData” to get the session object that must contain the variables idpRequest, idpMetadata
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", getParams, 1), SessionMngrResponse.class);
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
                redirectAttrs.addFlashAttribute("errorMsg", "None of the attributes requested are supported by this IdP!");
                return new ModelAndView("redirect:/eidas/authfail?reason=esmo");
            }

            model.addAttribute("sessionId", idpMsSessionId);
            model.addAttribute("login", "/eidas/login");
//            return new ModelAndView("redirect:/eidas/login?session=" + idpMsSessionId);
            return new ModelAndView("loginRedirect");

        } catch (NoSuchAlgorithmException e) {
            LOG.error("error sending getRequest");
            LOG.error(e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Internal Server Error");
            return new ModelAndView("redirect:/eidas/authfail?reason=esmo");
        } catch (IOException e) {
            LOG.error("error marshaling idpRequest from session");
            LOG.error(e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Internal Server Error");
            return new ModelAndView("redirect:/eidas/authfail?reason=esmo");
        } catch (NullPointerException e) {
            LOG.error("error getting external session from cache");
            redirectAttrs.addFlashAttribute("errorMsg", "error getting external session");
            LOG.error(e.getMessage());
            return new ModelAndView("redirect:/eidas/authfail?reason=esmo");
        }

    }

    @RequestMapping(value = "/idp/proceedAfterError", method = {RequestMethod.POST, RequestMethod.GET})
    public String returnAfterFailure(@RequestParam(value = "sessionId", required = true) String smSessionId, Model model) throws JsonProcessingException, IOException, NoSuchAlgorithmException {
        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        ObjectMapper mapper = new ObjectMapper();
        List<NameValuePair> requestParams = new ArrayList();
        String responseId = UUID.randomUUID().toString();
        ArrayList<AttributeType> emptyResponseAttributes = new ArrayList<>();
        Map<String, String> properties = new HashMap();
        AttributeSet receivedAttributes = AttributeSetFactory.make(responseId, TypeEnum.AUTHRESPONSE, "ACM001", "IdPms001", emptyResponseAttributes, properties);
        String attributSetString = mapper.writeValueAsString(receivedAttributes);
        UpdateDataRequest updateReq = new UpdateDataRequest(smSessionId, "dsResponse", attributSetString);
        SessionMngrResponse resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1),
                SessionMngrResponse.class);
        if (!resp.getCode().toString().equals("OK")) {
            LOG.error("ERROR: " + resp.getError());
            return "redirect:/eidas/authfail?reason=esmo";
        }
        //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken”
        requestParams.clear();
        requestParams.add(new NameValuePair("sessionId", smSessionId));
        requestParams.add(new NameValuePair("sender", paramServ.getParam("REDIRECT_JWT_SENDER"))); //[TODO] add correct sender "IdPms001"
        requestParams.add(new NameValuePair("receiver", paramServ.getParam("REDIRECT_JWT_RECEIVER"))); //"ACMms001"
        resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams, 1), SessionMngrResponse.class);
        String msToken = resp.getAdditionalData();
        model.addAttribute("msToken", msToken);
        model.addAttribute("acmUrl", configServ.getMsEndpointByIdAndApiCall(paramServ.getParam("ACM_ID"), "acmResponse"));
        return "acmRedirect";
    }

    private String checkEidasResponseForErrorCode(String eidasResponse, RedirectAttributes redirectAttrs) {
        if (eidasResponse.contains("202007") || eidasResponse.contains("202004")
                || eidasResponse.contains("202012") || eidasResponse.contains("202010")
                || eidasResponse.contains("003002")) {

            if (eidasResponse.contains("202007") || eidasResponse.contains("202012")) {
                LOG.debug("---------------202012!!!!!!!");
                redirectAttrs.addFlashAttribute("title", "Registration/Login Cancelled");
                redirectAttrs.addFlashAttribute("errorMsg", EIDAS_CONSENT_ERROR);
                return "redirect:" + System.getenv(URL_PREFIX) + "/eidas/authfail?reason=consent";

            }
            if (eidasResponse.contains("202004")) {
                redirectAttrs.addFlashAttribute("title", "Registration/Login Cancelled");
                redirectAttrs.addFlashAttribute("errorMsg", EIDAS_QAA_ERROR);
                return "redirect:" + System.getenv(URL_PREFIX) + "/eidas/authfail?reason=qa";

            }
            if (eidasResponse.contains("202010")) {
                redirectAttrs.addFlashAttribute("title", "Registration/Login Cancelled");
                redirectAttrs.addFlashAttribute("errorMsg", EIDAS_MISSING_ATTRIBUTE_ERROR);
                return "redirect:" + System.getenv(URL_PREFIX) + "/eidas/authfail?rearon=attr";

            }
            if (eidasResponse.contains("003002")) {
                redirectAttrs.addFlashAttribute("title", "Non-sucessful authentication");
                redirectAttrs.addFlashAttribute("errorMsg", "Please, return to the home page and re-initialize the process. If the authentication fails again, please contact your national eID provider");
                return "redirect:" + System.getenv(URL_PREFIX) + "/eidas/authfail?reason=fail";
            }
        }
        return null;
    }

}
