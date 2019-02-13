/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import eu.eidas.sp.SpEidasSamlTools;
import gr.uagean.loginWebApp.MemCacheConfig;
import gr.uagean.loginWebApp.model.pojo.LinkedInAuthAccessToken;
import gr.uagean.loginWebApp.service.CountryService;
import gr.uagean.loginWebApp.service.EidasPropertiesService;
import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.ParameterService;
import gr.uagean.loginWebApp.utils.LinkedInResponseParser;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;

/**
 *
 * @author nikos
 */
@Controller
public class ViewControllers {

    final static String EIDAS_URL = "EIDAS_NODE_URL";
    final static String SP_FAIL_PAGE = "SP_FAIL_PAGE";
    final static String SP_SUCCESS_PAGE = "SP_SUCCESS_PAGE";
    final static String SP_LOGO = System.getenv("SP_LOGO");

    final static String UAEGEAN_LOGIN = "UAEGEAN_LOGIN";
    final static String LINKED_IN_SECRET = "LINKED_IN_SECRET";
    final static String SECRET = "SP_SECRET";

    final static String CLIENT_ID = "CLIENT_ID";
    final static String REDIRECT_URI = "REDIRECT_URI";
    final static String HTTP_HEADER = "HTTP_HEADER";
    final static String URL_ENCODED = "URL_ENCODED";
    final static String URL_PREFIX = "URL_PREFIX";

    @Value("${eidas.error.consent}")
    private String EIDAS_CONSENT_ERROR;
    @Value("${eidas.error.qaa}")
    private String EIDAS_QAA_ERROR;
    @Value("${eidas.error.missing}")
    private String EIDAS_MISSING_ATTRIBUTE_ERROR;
    @Value("${eidas.error.consent.gr}")
    private String EIDAS_CONSENT_ERROR_GR;
    @Value("${eidas.error.qaa.gr}")
    private String EIDAS_QAA_ERROR_GR;
    @Value("${eidas.error.missing.gr}")
    private String EIDAS_MISSING_ATTRIBUTE_ERROR_GR;

    final static Logger LOG = LoggerFactory.getLogger(ViewControllers.class);

    @Autowired
    private EidasPropertiesService propServ;

    @Autowired
    private CountryService countryServ;

    @Autowired
    private KeyStoreService keyServ;

    @Autowired
    private ParameterService paramServ;

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping("/login")
    public ModelAndView loginView(HttpServletRequest request, @RequestParam String session) {

        ModelAndView mv = new ModelAndView("login");
        mv.addObject("nodeUrl", SpEidasSamlTools.getNodeUrl());
        mv.addObject("countries", countryServ.getEnabled());
        mv.addObject("spFailPage", System.getenv(SP_FAIL_PAGE));
        mv.addObject("spSuccessPage", System.getenv(SP_SUCCESS_PAGE));
        mv.addObject("logo", SP_LOGO);

        mv.addObject("legal", propServ.getLegalProperties());
        mv.addObject("natural", propServ.getNaturalProperties());
        String urlPrefix = StringUtils.isEmpty(paramServ.getParam(URL_PREFIX)) ? "" : paramServ.getParam(URL_PREFIX);

        boolean linkedIn = StringUtils.isEmpty(paramServ.getParam("LINKED_IN")) ? false : Boolean.parseBoolean(paramServ.getParam("LINKED_IN"));
        String clientID = paramServ.getParam(CLIENT_ID);
        String redirectURI = paramServ.getParam(REDIRECT_URI);
        String responseType = "code";
        String state = UUID.randomUUID().toString();
        mv.addObject("clientID", clientID);
        mv.addObject("redirectURI", redirectURI);
        mv.addObject("responseType", responseType);
        mv.addObject("state", state);
        mv.addObject("linkedIn", false);
        mv.addObject("uAegeanLogin", false);

        mv.addObject("urlPrefix", urlPrefix);

        Cache memCache = this.cacheManager.getCache(MemCacheConfig.IDPMS_SESSION);
        if (!StringUtils.isEmpty(session) && memCache != null && memCache.get(session) != null) {
            mv.addObject("IdPMSSession", (String) memCache.get(session).get());
        }

        // TODO hide stuff that are not related to esmo from the view
        return mv;
    }

    @RequestMapping("/authfail")
    public String authorizationFail(@RequestParam(value = "t", required = false) String token,
            @RequestParam(value = "reason", required = false) String reason,
            @CookieValue(value = "localeInfo", required = false) String langCookie,
            Model model) {

        if (reason != null) {
            model.addAttribute("title", "Registration/Login Cancelled");
            switch (reason) {
                case "disagree":
                    model.addAttribute("title", "Registration/Login termintated");
                    model.addAttribute("errorType", "DISAGREE");
                    break;
                case "consent":
                    model.addAttribute("errorMsg", EIDAS_CONSENT_ERROR);
                    break;
                case "qa":
                    model.addAttribute("errorMsg", EIDAS_QAA_ERROR);
                    break;
                case "attr":
                    model.addAttribute("errorMsg", EIDAS_MISSING_ATTRIBUTE_ERROR_GR);
                    break;
                case "fail":
                    model.addAttribute("title", "Non-sucessful authentication");
                    model.addAttribute("errorMsg", "Please, return to the home page and re-initialize the process. If the authentication fails again, please contact your national eID provider");
                    break;
                default:
                    model.addAttribute("errorType", "CANCEL");
                    break;
            }
        } else {
            model.addAttribute("title", "");
            if (langCookie != null && langCookie.equals("gr")) {
                model.addAttribute("errorMsg", "Η διαδικασία ταυτοποίησης ακυρώθηκε από το χρήστη");

            } else {
                model.addAttribute("errorMsg", "User Canceled Authentication process");

            }

        }

        String urlPrefix = StringUtils.isEmpty(paramServ.getParam(URL_PREFIX)) ? "" : paramServ.getParam(URL_PREFIX);
        model.addAttribute("urlPrefix", urlPrefix);

        model.addAttribute("server", System.getenv("SP_SERVER"));
        model.addAttribute("logo", SP_LOGO);

        return "authfail";
    }

    @RequestMapping(value = "/linkedInResponse", method = {RequestMethod.POST, RequestMethod.GET})
    public String linkedInResponse(@RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletResponse httpResponse) {

        //TODO Before you accept the authorization code, your application should ensure that the value returned in the state parameter matches the state value from your original authorization code request.
        if (org.apache.commons.lang3.StringUtils.isEmpty(error)) {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("grant_type", "authorization_code");
            map.add("code", code);
            map.add("redirect_uri", paramServ.getParam(REDIRECT_URI));
            map.add("client_id", paramServ.getParam(CLIENT_ID));
            map.add("client_secret", paramServ.getParam(LINKED_IN_SECRET));

            HttpEntity<MultiValueMap<String, String>> request
                    = new HttpEntity<>(map, headers);

            ResponseEntity<LinkedInAuthAccessToken> response = restTemplate
                    .exchange("https://www.linkedin.com/oauth/v2/accessToken", HttpMethod.POST, request, LinkedInAuthAccessToken.class
                    );

            // get User Data using accessToken 
            HttpHeaders headersUser = new HttpHeaders();
            headersUser.setContentType(MediaType.APPLICATION_JSON);
            headersUser.set("Authorization", "Bearer " + response.getBody().getAccess_token());
            HttpEntity<String> entity = new HttpEntity<String>("", headersUser);
            ResponseEntity<String> userResponse
                    = restTemplate.exchange("https://www.linkedin.com/v1/people/~:(id,firstName,lastName,email-address)?format=json",
                            HttpMethod.GET, entity, String.class
                    ); //user details https://www.linkedin.com/v1/people/~

            //return "token " + response.getBody().getAccess_token() + " , expires " + response.getBody().getExpires_in();
            //return userResponse.getBody();
            try {
                Map<String, String> jsonMap = LinkedInResponseParser.parse(userResponse.getBody());

                return "redirect:" + paramServ.getParam(SP_SUCCESS_PAGE);

            } catch (Exception e) {
                LOG.info("Exception", e);
            }

        }

        return "state" + state + " , code" + code;
    }

}
