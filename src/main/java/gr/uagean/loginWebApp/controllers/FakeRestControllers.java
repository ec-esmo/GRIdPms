/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.factory.AttributeSetFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.model.pojo.UpdateDataRequest;
import gr.uagean.loginWebApp.service.EsmoMetadataService;
import gr.uagean.loginWebApp.service.HttpSignatureService;
import gr.uagean.loginWebApp.service.KeyStoreService;
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
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

/**
 *
 * @author nikos
 */
@Controller
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST})
public class FakeRestControllers {

    @Autowired
    private ParameterService paramServ;

    private NetworkService netServ;
    private KeyStoreService keyServ;

    @Autowired
    private EsmoMetadataService metadataServ;

    private final static Logger LOG = LoggerFactory.getLogger(FakeRestControllers.class);

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
    public FakeRestControllers(KeyStoreService keyServ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnsupportedEncodingException, InvalidKeySpecException, IOException {
        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getSigningKey();
        String fingerPrint = "7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b";
        HttpSignatureService httpSigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(httpSigServ);
    }

    @RequestMapping(value = "/fakeSm/idp/authenticate", method = {RequestMethod.POST, RequestMethod.GET})
    public String fakeIdpResponse(@RequestParam(value = "msToken", required = true) String token, Model model) throws IOException, NoSuchAlgorithmException, KeyStoreException {

        ObjectMapper mapper = new ObjectMapper();
        String sessionMngrUrl = paramServ.getParam("SESSION_MANAGER_URL");
        List<NameValuePair> getParams = new ArrayList();
        getParams.add(new NameValuePair("token", token));

        SessionMngrResponse resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/validateToken", getParams), SessionMngrResponse.class);
        if (resp.getCode().toString().equals("OK") && StringUtils.isEmpty(resp.getError())) {
            String sessionId = resp.getSessionData().getSessionId();
            String idpMsSessionId = UUID.randomUUID().toString();

            //calls SM, “/sm/getSessionData” to get the session object that must contain the variables idpRequest, idpMetadata 
            getParams.clear();
            getParams.add(new NameValuePair("sessionId", sessionId));
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSessionData", getParams), SessionMngrResponse.class);
            String idpRequest = (String) resp.getSessionData().getSessionVariables().get("idpRequest");
            //TODO check the IDP metdata?
            String idpMetadata = (String) resp.getSessionData().getSessionVariables().get("idpMetadata");

            //check the idp requested attributes from the sesion object to see if there is a match
            AttributeSet idpRequestObject = mapper.readValue(idpRequest, AttributeSet.class);
            final Set<String> VALUES = new HashSet<>(Arrays.asList(
                    paramServ.getParam("EIDAS_PROPERTIES").trim().split(",")
            ));
            List<AttributeType> supportedAttrs = Arrays.stream(idpRequestObject.getAttributes()).filter(attribute -> {
                return VALUES.contains(attribute.getFriendlyName());
            }).collect(Collectors.toList());
            if (supportedAttrs.isEmpty()) {
                LOG.error("none of the attributes requested are supported!");
                return null;
            }
            //update the esmoGW session with the idp sessionId
            String eidasSession = "fakeEidasId" + UUID.randomUUID().toString();
            UpdateDataRequest updateReq = new UpdateDataRequest(sessionId, "GR_eIDAS_IdP_Session", eidasSession);
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json"), SessionMngrResponse.class);

            //receives response containing the external identifier and retrieves the internal identifier by calling, get “/sm/getSession”
            List<NameValuePair> requestParams = new ArrayList();
            requestParams.add(new NameValuePair("varName", "GR_eIDAS_IdP_Session"));
            requestParams.add(new NameValuePair("varValue", eidasSession));
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/getSession", requestParams), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                return null;
            }

            //IdP Connector updates the session with the variables received from the user authentication by calling the SM, with post, “/sm/updateSessionData” twice
            //once to store the received attributes
            String testResponse = "'AuthenticationResponse{id='_YLc6H3WhE2mjssJZHnJyOIvuRFBPIHsfszeGwVzAipyXS2csl7SlpVbKjUo4UOp', issuer='http://84.205.248.180:80/EidasNode/ConnectorResponderMetadata', status='ResponseStatus{failure='false', statusCode='urn:oasis:names:tc:SAML:2.0:status:Success', statusMessage='urn:oasis:names:tc:SAML:2.0:status:Success', subStatusCode='null'}', ipAddress='null', inResponseToId='_BJK3gNxljIfI.hOeabwBjO5ZFE54BPHQXmG9gEoXNb.BMgIN4LRZRzY18-ZyG6m', levelOfAssurance='http://eidas.europa.eu/LoA/low', attributes='{AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=true, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[cph8], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName', friendlyName='FirstName', personType=NaturalPerson, required=true, transliterationMandatory=true, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentGivenNameType', attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[cph8], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/DateOfBirth', friendlyName='DateOfBirth', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}DateOfBirthType', tributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValueMarshaller'}=[1966-01-01], AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier', friendlyName='PersonIdentifier', personType=NaturalPerson, required=true, transliterationMandatory=false, uniqueIdentifier=true, xmlType='{http://eidas.europa.eu/attributes/naturalperson}PersonIdentifierType',attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller'}=[CA/CA/Cph123456]}', audienceRestriction='http://138.68.103.237:8090/metadata', notOnOrAfter='2017-09-16T08:16:21.191Z', notBefore='2017-09-16T08:11:21.191Z', country='CA', encrypted='false'}'";
            AttributeSet receivedAttributes = AttributeSetFactory.makeFromEidasResponse("id", TypeEnum.Response, "issuer", "recipient", testResponse);
            String attributSetString = mapper.writeValueAsString(receivedAttributes);
            requestParams.clear();
            requestParams.add(new NameValuePair("dataObject", attributSetString));
            requestParams.add(new NameValuePair("variableName", "dsResponse"));
            requestParams.add(new NameValuePair("sessionId", resp.getSessionData().getSessionId()));
            String smSessionId = resp.getSessionData().getSessionId();
            updateReq = new UpdateDataRequest(smSessionId, "dsResponse", attributSetString);
            resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json"), SessionMngrResponse.class);

            if (!resp.getCode().toString().equals("OK")) {
                LOG.error("ERROR: " + resp.getError());
                return null;
            }

            // once to store the idp metadata
            requestParams.clear();
            if (metadataServ != null && metadataServ.getMetadata() != null) {
                updateReq = new UpdateDataRequest(smSessionId, "dsMetadata", mapper.writeValueAsString(metadataServ.getMetadata()));
                resp = mapper.readValue(netServ.sendPostBody(sessionMngrUrl, "/sm/updateSessionData", updateReq, "application/json"), SessionMngrResponse.class);
                if (!resp.getCode().toString().equals("OK")) {
                    LOG.error("ERROR: " + resp.getError());
                    return null;
                }
            }

            //IdP Connector generates a new security token to send to the ACM, by calling get “/sm/generateToken” 
            requestParams.clear();
            requestParams.add(new NameValuePair("sessionId", smSessionId));
            requestParams.add(new NameValuePair("sender", "ACMms001"));
            requestParams.add(new NameValuePair("receiver", "ACMms001"));
            resp = mapper.readValue(netServ.sendGet(sessionMngrUrl, "/sm/generateToken", requestParams), SessionMngrResponse.class);
            if (!resp.getCode().toString().equals("NEW")) {
                LOG.error("ERROR: " + resp.getError());
                return null;
            } else {
                String msToken = resp.getAdditionalData();
                //IdP calls, post  /acm/response
                String acmUrl = paramServ.getParam("ACM_URL");
                model.addAttribute("msToken", msToken);
                model.addAttribute("acmUrl", acmUrl + "/acm/response");
                return "acmRedirect";
            }

        }
        return null;
    }

}
