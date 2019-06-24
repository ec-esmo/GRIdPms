/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.eidas.sp.SpAuthenticationRequestData;
import gr.uagean.loginWebApp.MemCacheConfig;
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
import gr.uagean.loginWebApp.utils.EidasSpToolExtra;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

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

    private final static Logger Log = LoggerFactory.getLogger(RestControllers.class);

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
    public RestControllers(KeyStoreService keyServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getHttpSigningKey();
        String fingerPrint = DigestUtils.sha256Hex(this.keyServ.getHttpSigPublicKey().getEncoded());
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(this.keyServ);
    }

    @RequestMapping(value = "/generateSAMLToken", method = {RequestMethod.GET})
    public ResponseEntity getSAMLToken(@RequestParam(value = "citizenCountry", required = true) String citizenCountry,
            @RequestParam(value = "IdPMSSession", required = true) String idpSession) {

        String serviceProviderCountry = paramServ.getParam(SP_COUNTRY);
        ObjectMapper mapper = new ObjectMapper();
        try {
            ArrayList<String> pal = new ArrayList();
            pal.addAll(propServ.getEidasProperties());

            SpAuthenticationRequestData data;
            if (citizenCountry.equals("NO")) {
                data = EidasSpToolExtra.generateEIDASRequest(pal, citizenCountry, serviceProviderCountry, 3);
            } else {
                data = EidasSpToolExtra.generateEIDASRequest(pal, citizenCountry, serviceProviderCountry, 2);
            }
//            SpAuthenticationRequestData data
//                    = SpEidasSamlTools.generateEIDASRequest(pal, citizenCountry, serviceProviderCountry);

            // get and store external session Id to the SM ms
            Cache memcache = this.cacheManager.getCache(MemCacheConfig.IDPMS_SESSION);
            if (memcache == null) {
                throw new NullPointerException("error getting ipd cache");
            } else {
                String esmoGWSession = (String) memcache.get(idpSession).get();
                String eidasSession = data.getID();
                String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
                UpdateDataRequest updateReq = new UpdateDataRequest(esmoGWSession, "GR_eIDAS_IdP_Session", eidasSession);
                Log.info("Storing GR_eIDAS_IdP_Session" + eidasSession);
                SessionMngrResponse resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json", 1), SessionMngrResponse.class);
                if (resp.getCode().toString().equals("OK")) {
                    // IdP Connector generates “external session identifier” for the authentication (e.g. eIDAS uuid) and stores it in the internal session by calling post, “/sm/updateSessionData”
                    return ResponseEntity.ok(data.getSaml());
                }
            }
        } catch (NullPointerException e) {
            Log.error("NulPointer Caught", e);
        } catch (IOException ex) {
            Log.error("io Caught", ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.error("NoSuchAlgorithmException Caught", ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @RequestMapping(value = "/idp/authenticate", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView authenticate(@RequestParam(value = "msToken", required = true) String token,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs, Model model
    ) {

        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.FOUND);
        ObjectMapper mapper = new ObjectMapper();
        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        List<NameValuePair> getParams = new ArrayList();
        getParams.add(new NameValuePair("token", token));

        try {
            //calls SM, get /sm/validateToken, to validate the received token and get the sessionId
            SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/validateToken", getParams, 1), SessionMngrResponse.class);
            if (resp.getCode().toString().equals("OK") && StringUtils.isEmpty(resp.getError())) {
                String sessionId = resp.getSessionData().getSessionId();
//                redirectAttrs.addFlashAttribute("esmoSessionId", sessionId);
                model.addAttribute("esmoSessionId", sessionId);

                getParams.clear();
                getParams.add(new NameValuePair("sessionId", sessionId));
                resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", getParams, 1), SessionMngrResponse.class);
                String spOrigin = (String) resp.getSessionData().getSessionVariables().get("SP_ORIGIN");
                Log.info("got SP origin:" + spOrigin);
                if (StringUtils.isEmpty(spOrigin) || spOrigin.equals("GR")) {
                    Log.info("redirecting to :" + spOrigin);
                    model.addAttribute("login", "/eidas/gr/idp/authenticate");
                    return new ModelAndView("authenticateRedirect");
//                    return new RedirectView("/eidas/gr/idp/authenticate");
                } else {

                    if (spOrigin.equals("NO")) {
                        Log.info("redirecting to :" + spOrigin);
                        model.addAttribute("login", "/eidas/no/idp/authenticate");
                        return new ModelAndView("authenticateRedirect");
//                        return new RedirectView("/eidas/no/idp/authenticate");
                    } else {
                        Log.info("redirecting to production:" + spOrigin);
                        model.addAttribute("login", "/eidas/no/prod/idp/authenticate");
                        return new ModelAndView("authenticateRedirect");
//                        return new RedirectView("/eidas/no/prod/idp/authenticate");
                    }

                }

            }
        } catch (Exception e) {
            Log.error(e.getMessage());
        }

        return null;
    }

}
