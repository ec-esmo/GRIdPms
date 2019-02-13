/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.service.HttpSignatureService;
import gr.uagean.loginWebApp.service.NetworkService;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author nikos
 */

public class NetworkServiceImpl implements NetworkService {

    private HttpSignatureService sigServ;
    private final static Logger LOG = LoggerFactory.getLogger(NetworkServiceImpl.class);

    public NetworkServiceImpl(HttpSignatureService sigServ) {
        this.sigServ = sigServ;
    }

    @Override
    public String sendPostBody(String hostUrl, String uri, Object postBody, String contentType) throws IOException, NoSuchAlgorithmException {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z",Locale.ENGLISH);
        String nowDate = formatter.format(date);
        String requestId = UUID.randomUUID().toString();

        ObjectMapper mapper = new ObjectMapper();
        String updateString = mapper.writeValueAsString(postBody);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(updateString.getBytes()); // post parameters are added as uri parameters not in the body when form-encoding
        String host = hostUrl.replace("http://", "").replace("https://", "");
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("authorization", sigServ.generateSignature(host, "POST", "/sm/updateSessionData", postBody, "application/json;charset=UTF-8", requestId));
            requestHeaders.add("host", hostUrl);
            requestHeaders.add("original-date", nowDate);
            requestHeaders.add("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(MessageDigest.getInstance("SHA-256").digest(updateString.getBytes()))));
            requestHeaders.add("x-request-id", requestId);
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            requestHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + uri);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Object> requestEntity = new HttpEntity<>(postBody, requestHeaders);
            ResponseEntity<String> response
                    = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, requestEntity,
                            String.class);
            return response.getBody();
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        return null;
    }

    @Override
    public String sendPostForm(String hostUrl, String uri,
            List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException {

        Map<String, String> map = new HashMap();
        MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();

        urlParameters.stream().forEach(nameVal -> {
            map.put(nameVal.getName(), nameVal.getValue());
            multiMap.add(nameVal.getName(), nameVal.getValue());
        });

        String requestId = UUID.randomUUID().toString();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String host = hostUrl.replace("http://", "").replace("https://", "");

        try {
            headers.add("authorization", sigServ.generateSignature(host, "POST", uri, null, "application/x-www-form-urlencoded", requestId));
            Date date = new Date();
            byte[] digestBytes;
            //only when the request is json encoded are the post params added to the body of the request
            // else they eventually become encoded to the url
            digestBytes = MessageDigest.getInstance("SHA-256").digest("".getBytes());
            addHeaders(headers, host, date, digestBytes, uri, requestId);

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOG.error("could not generate signature!!");
            LOG.error(e.getMessage());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(multiMap, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                hostUrl + uri, request, String.class);

        assert (response.getStatusCode().equals(HttpStatus.CREATED) || response.getStatusCode().equals(HttpStatus.OK));
        return response.getBody();
    }

    @Override
    public String sendGet(String hostUrl, String uri,
            List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z",Locale.ENGLISH);
        String nowDate = formatter.format(date);
        String requestId = UUID.randomUUID().toString();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + uri);
        if (urlParameters != null) {
            Map<String, String> map = new HashMap();
            urlParameters.stream().forEach(nameVal -> {
                map.put(nameVal.getName(), nameVal.getValue());
                builder.queryParam(nameVal.getName(), nameVal.getValue());
            });
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        String host = hostUrl.replace("http://", "").replace("https://", "");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("".getBytes());
        try {
            requestHeaders.add("host", host);
            requestHeaders.add("original-date", nowDate);
            requestHeaders.add("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digest)));
            requestHeaders.add("x-request-id", requestId);
            URL url = new URL(builder.toUriString());

            requestHeaders.add("authorization", sigServ.generateSignature(host, "GET", url.getPath()+"?"+url.getQuery(), null, "application/x-www-form-urlencoded", requestId));
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LOG.error("could not generate signature!!");
            LOG.error(e.getMessage());
        }

        HttpEntity entity = new HttpEntity(requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    

    private void addHeaders(HttpHeaders headers, String host, Date date, byte[] digestBytes, String uri, String requestId) throws NoSuchAlgorithmException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss z",Locale.ENGLISH);
        String nowDate = formatter.format(date);
        headers.add("host", host);
        headers.add("original-date", nowDate);
        headers.add("digest", "SHA-256=" + new String(org.tomitribe.auth.signatures.Base64.encodeBase64(digestBytes)));
        headers.add("x-request-id", requestId);

    }

}
