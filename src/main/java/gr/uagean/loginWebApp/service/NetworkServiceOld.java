/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service;

import gr.uagean.loginWebApp.model.pojo.SessionMngrResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.apache.commons.httpclient.NameValuePair;


/**
 *
 * @author nikos
 */
public interface NetworkServiceOld {

    public SessionMngrResponse sendPostForm(String hostUrl, String uri, List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException;
    public String sendPostFormStringResponse(String hostUrl, String uri, List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException;
    public SessionMngrResponse sendPostBody(String hostUrl, String uri, Object postBody, String contentType) throws IOException, NoSuchAlgorithmException;
    
    public SessionMngrResponse sendGet(String hostUrl, String uri, List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException;
    public String sendGetStringResponse(String hostUrl, String uri, List<NameValuePair> urlParameters) throws IOException, NoSuchAlgorithmException;

}
