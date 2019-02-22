/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.loginWebApp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.LoginWebAppApplication;
import gr.uagean.loginWebApp.MemCacheConfig;
//import gr.uagean.loginWebApp.MemCacheConfig;
import gr.uagean.loginWebApp.TestRestControllersConfig;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import gr.uagean.loginWebApp.model.pojo.UpdateDataRequest;
import gr.uagean.loginWebApp.service.HttpSignatureService;
import gr.uagean.loginWebApp.service.KeyStoreService;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.NameValuePair;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import gr.uagean.loginWebApp.service.impl.HttpSignatureServiceImpl;
import gr.uagean.loginWebApp.service.impl.NetworkServiceImpl;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import org.junit.Before;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LoginWebAppApplication.class, TestRestControllersConfig.class, MemCacheConfig.class})
@AutoConfigureMockMvc
public class TestSessisonManagerConnection {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private KeyStoreService keyServ;

    private NetworkServiceImpl netServ;
    private ObjectMapper mapper;

    @Before
    public void init() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        mapper = new ObjectMapper();
        Key signingKey = this.keyServ.getSigningKey();
        String fingerPrint = "7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b";
        HttpSignatureService sigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        netServ = new NetworkServiceImpl(sigServ);
    }

//    @Test
    public void testStartSMSession() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, KeyStoreException, UnrecoverableKeyException {
        String hostUrl = "http://0.0.0.0:8090";
        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = this.mapper.readValue(netServ.sendPostForm(hostUrl, uri, postParams), SessionMngrResponse.class);
        System.out.println(resp.getCode());
        System.out.println(resp.getSessionData().getSessionId());

        assertEquals(resp.getCode().toString(), "NEW");
        Assert.assertNotNull(resp.getSessionData().getSessionId());

    }

//    @Test
    public void testUpdateSessionData() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8090";
        AttributeType attributes = new AttributeType("name", "CurrentGivenName", "UTF-8", "en EN", false, new String[]{"NIKOS"});
        AttributeSet attrSet = new AttributeSet("uuid", TypeEnum.Request, "issuer", "recipient", new AttributeType[]{attributes}, null,null,"low",null,null,null);

        ObjectMapper mapper = new ObjectMapper();
        String attrSetString = mapper.writeValueAsString(attrSet);

        String uri = "/sm/updateSessionData";
        List<NameValuePair> postParams = new ArrayList();
        postParams.add(new NameValuePair("sessionId", "182b548a-af37-41fd-8c6f-1a797eda2a0b"));
        postParams.add(new NameValuePair("dataObject", attrSetString));
        postParams.add(new NameValuePair("variableName", "idpRequest"));

        UpdateDataRequest updateReq = new UpdateDataRequest("182b548a-af37-41fd-8c6f-1a797eda2a0b", "idpRequest", attrSetString);

        SessionMngrResponse resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, "/sm/updateSessionData", updateReq, "application/json"), SessionMngrResponse.class);
        System.out.println(resp.getCode());

        assertEquals(resp.getCode().toString(), "OK");

    }

//    @Test
    public void testGetSMSession() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8090";
//        String uri="/sm/getSession";

        String uri = "/sm/getSessionData";
        List<NameValuePair> getParams = new ArrayList();
        NameValuePair val = new NameValuePair("sessionId", "182b548a-af37-41fd-8c6f-1a797eda2a0b");
        getParams.add(val);

        SessionMngrResponse resp = this.mapper.readValue(netServ.sendGet(hostUrl, uri, getParams), SessionMngrResponse.class);
        System.out.println(resp.getCode());
        assertEquals(resp.getCode().toString(), "OK");

    }

//    @Test
    public void testGenerateToken() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8090";
        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = this.mapper.readValue(netServ.sendPostForm(hostUrl, uri, postParams), SessionMngrResponse.class);
        System.out.println(resp.getCode());
        String sessionId = resp.getSessionData().getSessionId();

        AttributeType attributes = new AttributeType("name", "CurrentGivenName", "UTF-8", "en EN", false, new String[]{"NIKOS"});
        AttributeSet attrSet = new AttributeSet("uuid", TypeEnum.Request, "issuer", "recipient", new AttributeType[]{attributes}, null,null,"low",null,null,null);

        ObjectMapper mapper = new ObjectMapper();
        String attrSetString = mapper.writeValueAsString(attrSet);

//        uri = "/sm/updateSessionData";
        postParams.clear();
        postParams.add(new NameValuePair("sessionId", sessionId));
        postParams.add(new NameValuePair("dataObject", attrSetString));
        postParams.add(new NameValuePair("variableName", "idpRequest"));

        UpdateDataRequest updateReq = new UpdateDataRequest(sessionId, "idpRequest", attrSetString);
        resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, "/sm/updateSessionData", updateReq, "application/json;charset=UTF-8"), SessionMngrResponse.class);

        assertEquals(resp.getCode().toString(), "OK");

        uri = "/sm/generateToken";
        List<NameValuePair> getParams = new ArrayList();
        NameValuePair val = new NameValuePair("sessionId", sessionId);
        getParams.add(val);
        getParams.add(new NameValuePair("receiver", "ACMms001"));
        getParams.add(new NameValuePair("sender", "ACMms001"));

        resp = this.mapper.readValue(netServ.sendGet(hostUrl, uri, getParams), SessionMngrResponse.class);
        System.out.println(resp.getCode());
        System.out.println(resp.getAdditionalData());

        Assert.assertNotNull(resp.getAdditionalData());

    }

    @Test
    public void testFullFlow() throws IOException, NoSuchAlgorithmException, Exception {
        String hostUrl = "http://0.0.0.0:8090";
//        String hostUrl = "http://5.79.83.118:8090";
        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = this.mapper.readValue(netServ.sendPostForm(hostUrl, uri, postParams), SessionMngrResponse.class);
        System.out.println(resp.getCode());
        String sessionId = resp.getSessionData().getSessionId();

        AttributeType[] attrType = new AttributeType[1];
        String[] values = new String[1];
        AttributeType att1 = new AttributeType("someURI", "FirstName", "UTF-8", "en", true, values);
        attrType[0] = att1;
        AttributeSet attrSet = new AttributeSet("id", TypeEnum.Request, "ACMms001", "IDPms001", attrType, new HashMap<>(),null,"low",null,null,null);

        ObjectMapper mapper = new ObjectMapper();
        String attrSetString = mapper.writeValueAsString(attrSet);
        uri = "/fakeSm/updateSessionData";
        UpdateDataRequest updateDR = new UpdateDataRequest();
        updateDR.setSessionId(sessionId);
        updateDR.setVariableName("idpRequest");
        updateDR.setDataObject(attrSetString);
        resp = this.mapper.readValue(netServ.sendPostBody(hostUrl, uri, updateDR, "application/json"), SessionMngrResponse.class);

        uri = "/sm/generateToken";
        postParams.clear();
        postParams.add(new NameValuePair("sessionId", sessionId));
        postParams.add(new NameValuePair("sender", "ACMms001"));
        postParams.add(new NameValuePair("receiver", "IDPms001"));
        resp = this.mapper.readValue(netServ.sendGet(hostUrl, uri, postParams), SessionMngrResponse.class);
        String token = resp.getAdditionalData();

        mvc.perform(get("/fakeSm//idp/authenticate?msToken=" + token))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

}
