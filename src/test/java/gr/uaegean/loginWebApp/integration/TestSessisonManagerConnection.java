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
import gr.uagean.loginWebApp.service.NetworkServiceOld;

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
    private NetworkServiceOld netServ;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testStartSMSession() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8090";
        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = netServ.sendPostForm(hostUrl, uri, postParams);
        System.out.println(resp.getCode());
        System.out.println(resp.getSessionData().getSessionId());

        assertEquals(resp.getCode().toString(), "NEW");
        Assert.assertNotNull(resp.getSessionData().getSessionId());

    }

//    @Test
    public void testUpdateSessionData() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8090";
        AttributeType attributes = new AttributeType("name", "CurrentGivenName", "UTF-8", "en EN", false, new String[]{"NIKOS"});
        AttributeSet attrSet = new AttributeSet("uuid", TypeEnum.Request, "issuer", "recipient", new AttributeType[]{attributes}, null);

        ObjectMapper mapper = new ObjectMapper();
        String attrSetString = mapper.writeValueAsString(attrSet);

        String uri = "/sm/updateSessionData";
        List<NameValuePair> postParams = new ArrayList();
        postParams.add(new NameValuePair("sessionId", "182b548a-af37-41fd-8c6f-1a797eda2a0b"));
        postParams.add(new NameValuePair("dataObject", attrSetString));
        postParams.add(new NameValuePair("variableName", "idpRequest"));

        UpdateDataRequest updateReq = new UpdateDataRequest("182b548a-af37-41fd-8c6f-1a797eda2a0b", "idpRequest", attrSetString);

        SessionMngrResponse resp = netServ.sendPostBody(hostUrl, "/sm/updateSessionData", updateReq, "application/json");
        System.out.println(resp.getCode());

        assertEquals(resp.getCode().toString(), "OK");

    }

    @Test
    public void testGetSMSession() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8080";
//        String uri="/sm/getSession";

        String uri = "/sm/getSessionData";
        List<NameValuePair> getParams = new ArrayList();
        NameValuePair val = new NameValuePair("sessionId", "182b548a-af37-41fd-8c6f-1a797eda2a0b");
        getParams.add(val);

        SessionMngrResponse resp = netServ.sendGet(hostUrl, uri, getParams);
        System.out.println(resp.getCode());
        assertEquals(resp.getCode().toString(), "OK");

    }

    @Test
    public void testGenerateToken() throws IOException, NoSuchAlgorithmException {
        String hostUrl = "http://0.0.0.0:8090";
        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = netServ.sendPostForm(hostUrl, uri, postParams);
        System.out.println(resp.getCode());
        String sessionId = resp.getSessionData().getSessionId();

        AttributeType attributes = new AttributeType("name", "CurrentGivenName", "UTF-8", "en EN", false, new String[]{"NIKOS"});
        AttributeSet attrSet = new AttributeSet("uuid", TypeEnum.Request, "issuer", "recipient", new AttributeType[]{attributes}, null);

        ObjectMapper mapper = new ObjectMapper();
        String attrSetString = mapper.writeValueAsString(attrSet);

//        uri = "/sm/updateSessionData";
        postParams.clear();
        postParams.add(new NameValuePair("sessionId", sessionId));
        postParams.add(new NameValuePair("dataObject", attrSetString));
        postParams.add(new NameValuePair("variableName", "idpRequest"));

        UpdateDataRequest updateReq = new UpdateDataRequest(sessionId, "idpRequest", attrSetString);
        resp = netServ.sendPostBody(hostUrl, "/sm/updateSessionData", updateReq, "application/json;charset=UTF-8");

        assertEquals(resp.getCode().toString(), "OK");

        uri = "/sm/generateToken";
        List<NameValuePair> getParams = new ArrayList();
        NameValuePair val = new NameValuePair("sessionId", sessionId);
        getParams.add(val);
        getParams.add(new NameValuePair("receiver", "ACMms001"));
        getParams.add(new NameValuePair("sender", "ACMms001"));

        resp = netServ.sendGet(hostUrl, uri, getParams);
        System.out.println(resp.getCode());
        System.out.println(resp.getAdditionalData());

        Assert.assertNotNull(resp.getAdditionalData());

    }

    @Test
    public void testFullFlow() throws IOException, NoSuchAlgorithmException, Exception {
        String hostUrl = "http://0.0.0.0:8090";
        String uri = "/sm/startSession";
        List<NameValuePair> postParams = new ArrayList();

        SessionMngrResponse resp = netServ.sendPostForm(hostUrl, uri, postParams);
        System.out.println(resp.getCode());
        String sessionId = resp.getSessionData().getSessionId();

        mvc.perform(get("/fakeSm//idp/authenticate?msToken="+sessionId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

    }

}
