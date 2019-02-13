/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.controllers;

import com.google.common.io.ByteStreams;
import gr.uagean.loginWebApp.service.ParameterService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import gr.uagean.loginWebApp.service.HttpSignatureServiceOld;

/**
 *
 * @author nikos
 */
@Controller
public class TestControllers {

    @Autowired
    private ParameterService paramServ;
    @Autowired
    private HttpSignatureServiceOld sigServ;

    private final static Logger LOG = LoggerFactory.getLogger(TestControllers.class);

    @RequestMapping(value = "/external", produces = MediaType.TEXT_HTML_VALUE)
    public void getExternalPage(@RequestParam("url") String url, HttpServletResponse response) throws IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, UnrecoverableKeyException, UnsupportedEncodingException {

        String requestId = UUID.randomUUID().toString();
        String acmUrl = paramServ.getParam("ACM_URL");
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        Date date = new Date();
        String nowDate = formatter.format(date);
        Map<String, String> map = new HashMap();
        map.put("msToken", "TOKEN");
        //only when the request is json encoded are the post params added to the body of the request
        // else they eventually become encoded to the url
        byte[] digestBytes = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String digest = "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digestBytes));
        String requestTarget = "GET" + " " + "/acm/response";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.addHeader("host", acmUrl);
        request.addHeader("request-target", requestTarget);
        request.addHeader("original-date", nowDate);
        request.addHeader("digest", digest);
        request.addHeader("x-request-id", requestId);
        request.addHeader("authorization", sigServ.generateSignature(acmUrl, "POST", "/acm/response", map, "application/x-www-form-urlencoded", requestId));

        HttpResponse response1 = client.execute(request);
        response.setContentType("text/html");
        ByteStreams.copy(response1.getEntity().getContent(), response.getOutputStream());
    }

    @RequestMapping(value = "/starTestRedirect", method = {RequestMethod.GET})
    public String testPOST2POSTRedirectSend(@RequestParam(value = "test1", required = false) String test2, HttpServletResponse response, HttpServletRequest request, Model model) throws IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, ServletException, URISyntaxException {
        Map<String, String> map = new HashMap();
        map.put("msToken", "TOKEN");
        String requestId = UUID.randomUUID().toString();
        String acmUrl = paramServ.getParam("ACM_URL");
        model.addAttribute("msToken", "TOKEN");
        model.addAttribute("authorizationHeader", sigServ.generateSignature(acmUrl, "GET", "/acm/response", map, "application/x-www-form-urlencoded", requestId));

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z");
        Date date = new Date();
        String nowDate = formatter.format(date);
        //only when the request is json encoded are the post params added to the body of the request
        // else they eventually become encoded to the url
        byte[] digestBytes = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        String digest = "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digestBytes));
        String requestTarget = "GET" + " " + "/acm/response";

        //only when the request is json encoded are the post params added to the body of the request
        // else they eventually become encoded to the url
        try {
            response.setHeader("host", acmUrl);
            response.setHeader("(request-target)", requestTarget);
            response.setHeader("original-date", nowDate);
            response.setHeader("digest", digest);
            response.setHeader("x-request-id", requestId);
            response.setHeader("authorization", sigServ.generateSignature(acmUrl, "POST", "/acm/response", map, "application/x-www-form-urlencoded", requestId));

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOG.error("could not generate signature!!");
            LOG.error(e.getMessage());
        }

//        return "redirect:" + "/starTestReceive?msToken=" + "token";
//        RequestDispatcher rd =  getServletContext()
//      .getRequestDispatcher("http://localhost:8080//starTestReceive?msToken=token");
//        rd.forward(request, response);
        URI yahoo = new URI("http://localhost:8080//starTestReceive?msToken=token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(yahoo);
        httpHeaders.add("host", acmUrl);
        httpHeaders.add("(request-target)", requestTarget);
        httpHeaders.add("original-date", nowDate);
        httpHeaders.add("digest", digest);
        httpHeaders.add("x-request-id", requestId);
        httpHeaders.add("authorization", sigServ.generateSignature(acmUrl, "POST", "/acm/response", map, "application/x-www-form-urlencoded", requestId));

        return "forward:/external?url=http%3A%2F%2Flocalhost%3A8080%2FstarTestReceive%3FmsToken%3D123";

    }
    
    
    
    
    @RequestMapping(value = "/starTestRedirect2", method = {RequestMethod.GET})
    public String testPOST2POSTWithForm(@RequestParam(value = "test1", required = false) String test2, HttpServletResponse response, HttpServletRequest request, Model model) throws IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, ServletException, URISyntaxException {
        
        model.addAttribute("msToken", "TOKEN");
        model.addAttribute("acmUrl", "/starTestReceive");
        return "acmRedirect";

    }
    
    
    

    @RequestMapping(value = "/starTestReceive", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody
    String testPOST2POSTRedirectReceive(@RequestParam(value = "msToken", required = false) String test2, HttpServletResponse response,
            HttpServletRequest request, Model model) throws IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        return test2 + " " + request.getHeader("authorization");
    }

}
