/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.MemCacheConfig;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.factory.AttributeSetFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.LinkedInAuthAccessToken;
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
import java.util.HashSet;
import java.util.List;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

/**
 *
 * @author nikos
 */
@Controller
public class NOControllers {

    @Autowired
    private EidasPropertiesService propServ;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ParameterService paramServ;

    private NetworkService netServ;
    private KeyStoreService keyServ;

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

    private final static Logger LOG = LoggerFactory.getLogger(NOControllers.class);

    private final static String SP_COUNTRY = "SP_COUNTRY";
    private final static String SP_FAIL_PAGE = "SP_FAIL_PAGE";

    final static String UAEGEAN_LOGIN = "UAEGEAN_LOGIN";
    final static String LINKED_IN_SECRET = "LINKED_IN_SECRET";

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    final static String NO_REDIRECT_URL = "NO_REDIRECT_URL";
    final static String NO_CLIENT_ID = "NO_CLIENT_ID";
    final static String NO_CLIENT_SECRET = "NO_CLIENT_SECRET";
    final static String NO_TOKEN_URL = "NO_TOKEN_URL";

    @Autowired
    public NOControllers(KeyStoreService keyServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getHttpSigningKey();
        String fingerPrint = DigestUtils.sha256Hex(this.keyServ.getHttpSigPublicKey().getEncoded());
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(this.keyServ);
    }

    @RequestMapping(value = "/no/idp/authenticate", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(HttpServletRequest request, RedirectAttributes redirectAttrs, Model model,
            @RequestParam(value = "sessionId", required = false) String sessionId) {

        request.setAttribute(
                View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.FOUND);

        ObjectMapper mapper = new ObjectMapper();

        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        String noReturnUrl = paramServ.getParam(NO_REDIRECT_URL);
        String noClientId = paramServ.getParam("NO_CLIENT_ID");
        String noAuthURL = paramServ.getParam("NO_AUTH_URL");

        List<NameValuePair> getParams = new ArrayList();
//        getParams.add(new NameValuePair("token", token));
        SessionMngrResponse resp;
        try {
            //calls SM, get /sm/validateToken, to validate the received token and get the sessionId
//            String sessionId = (String) model.asMap().get("esmoSessionId");
            LOG.debug("SM sessionId " + sessionId);

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

            LOG.debug("call getSessionData");
            //calls SM, “/sm/getSessionData” to get the session object that must contain the variables idpRequest, idpMetadata
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", getParams, 1), SessionMngrResponse.class);
            String idpRequest = (String) resp.getSessionData().getSessionVariables().get("idpRequest");
            LOG.debug("idp request" + idpRequest);

            //TODO check the IDP metdata?
            String idpMetadata = (String) resp.getSessionData().getSessionVariables().get("idpMetadata");

            AttributeSet idpRequestObject = mapper.readValue(idpRequest, AttributeSet.class);
            final Set<String> VALUES = new HashSet<>(Arrays.asList(
                    paramServ.getParam("NO_EIDAS_PROPERTIES").trim().split(",")
            ));
            LOG.debug("idp VALUES" + VALUES.toString());

            List<AttributeType> supportedAttrs = Arrays.stream(idpRequestObject.getAttributes()).filter(attribute -> {
                return VALUES.contains(attribute.getFriendlyName());
            }).collect(Collectors.toList());

            if (supportedAttrs.isEmpty()) {
                LOG.error("none of the attributes requested are supported!");
                return new ModelAndView("redirect:/authfail");
            }
            LOG.debug("redirect to dataporten");

            String dataPortenAuthUrl
                    = noAuthURL //"https://auth.dataporten-test.uninett.no/oauth/authorization?"
                    + "response_type=code"
                    + "&client_id=" + noClientId //0383672b-b749-4d13-8f37-ef0892d5d439"
                    //                        + "&redirect_uri=http://dss.aegean.gr:8092/idp/oidc"
                    + "&redirect_uri=" + noReturnUrl //http://localhost:8080/idp/oidc"
                    //                        + "&scope=openid%20profile%20eidas"
                    + "&eidas"
                    + "&state=" + sessionId;

            //TODO
            // add as env variables:
            /*
                NO_AUTH_URL=https://auth.dataporten-test.uninett.no/oauth/authorization?
                NO_TOKEN_URL=https://auth.dataporten-test.uninett.no/oauth/token
                NO_REDIRECT_URL=http://dss.aegean.gr:8092/idp/oidc
                NO_CLIENT_ID=0383672b-b749-4d13-8f37-ef0892d5d439
                NO_CLIENT_SECRET=fc1556d5-2a49-44f8-90ac-7be62516922b
                NO_USER_INFO_URL=https://auth.dataporten-test.uninett.no/openid/userinfo


             */
            return new ModelAndView("redirect:" + dataPortenAuthUrl);

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

    @RequestMapping(value = "/idp/oidc", method = {RequestMethod.POST, RequestMethod.GET})
    public String getNorweigianEidasResponse(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletResponse httpResponse,
            Model model,
            RedirectAttributes redirectAttrs) throws IOException {

        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");

        if (StringUtils.isEmpty(error)) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("grant_type", "authorization_code");
                map.add("code", code);
                map.add("redirect_uri", paramServ.getParam(NO_REDIRECT_URL));
                map.add("client_id", paramServ.getParam(NO_CLIENT_ID));
                map.add("client_secret", paramServ.getParam(NO_CLIENT_SECRET));

                HttpEntity<MultiValueMap<String, String>> request
                        = new HttpEntity<>(map, headers);
                //get the access token from the access code!
                ResponseEntity<LinkedInAuthAccessToken> accessTokenResponse = restTemplate
                        .exchange(paramServ.getParam(NO_TOKEN_URL), HttpMethod.POST, request, LinkedInAuthAccessToken.class);

                LOG.info("access token is " + accessTokenResponse.getBody().getAccess_token());
                // get User Data using accessToken
                headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + accessTokenResponse.getBody().getAccess_token());
                HttpEntity<String> entity = new HttpEntity<>("", headers);
                ResponseEntity<String> userResponse
                        = restTemplate.exchange(this.paramServ.getParam("NO_USER_INFO_URL"),
                                HttpMethod.GET, entity, String.class);

                //IdP Connector updates the session with the variables received from the user authentication by calling the SM, with post, “/sm/updateSessionData” twice
                //once to store the received attributes
                String id = UUID.randomUUID().toString();
                AttributeSet receivedAttributes = AttributeSetFactory.makeFromNoResponse(id, TypeEnum.AUTHRESPONSE, "issuer", "recipient", userResponse.getBody());
                ObjectMapper mapper = new ObjectMapper();
                String attributSetString = mapper.writeValueAsString(receivedAttributes);
                List<NameValuePair> requestParams = new ArrayList();
                requestParams.clear();
                requestParams.add(new NameValuePair("dataObject", attributSetString));
                requestParams.add(new NameValuePair("variableName", "dsResponse"));
                requestParams.add(new NameValuePair("sessionId", state));
                UpdateDataRequest updateReq = new UpdateDataRequest(state, "dsResponse", attributSetString);

                //update attribuetes received
                SessionMngrResponse resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
                LOG.debug("updateSessionData " + resp.getCode().toString());
                if (!resp.getCode().toString().equals("OK")) {
                    LOG.error("ERROR: " + resp.getError());
                    redirectAttrs.addFlashAttribute("errorMsg", "error communicating with ESMO GW");
                    return "redirect:/authfail?reason=esmo";
                }
                //update with metadata
                requestParams.clear();
                updateReq = new UpdateDataRequest(state, "dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata()));
                resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
                LOG.info("updateSessionData metadata " + resp.getCode().toString());
                if (!resp.getCode().toString().equals("OK")) {
                    LOG.error("ERROR: " + resp.getError());
                    redirectAttrs.addFlashAttribute("errorMsg", "error communicating with ESMO GW");
                    return "redirect:/authfail?reason=esmo";
                }

                //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken”
                requestParams.clear();
                requestParams.add(new NameValuePair("sessionId", state));
                requestParams.add(new NameValuePair("sender", paramServ.getParam("REDIRECT_JWT_SENDER"))); //[TODO] add correct sender "IdPms001"
                requestParams.add(new NameValuePair("receiver", paramServ.getParam("REDIRECT_JWT_RECEIVER"))); //"ACMms001"
                resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams, 1), SessionMngrResponse.class);
                LOG.info("generateToken" + resp.getCode().toString());
                if (!resp.getCode().toString().equals("NEW")) {
                    LOG.error("ERROR: " + resp.getError());
                    redirectAttrs.addAttribute("errorMsg", "error communicating with ESMO GW");
                    return "redirect:/authfail?reason=esmo";
                } else {
                    String msToken = resp.getAdditionalData();
                    model.addAttribute("msToken", msToken);
                    model.addAttribute("acmUrl", configServ.getMsEndpointByIdAndApiCall(paramServ.getParam("ACM_ID"), "acmResponse"));
                    return "acmRedirect";
                }

            } catch (NoSuchAlgorithmException ex) {
                LOG.error(ex.getMessage());
                return "redirect:/authfail";
            } catch (KeyStoreException ex) {
                LOG.error(ex.getMessage());
                return "redirect:/authfail";
            }
        }

        redirectAttrs.addAttribute("errorMsg", errorDescription);
        return "redirect:/authfail";
    }

}
