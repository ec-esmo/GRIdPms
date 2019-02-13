/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service.impl;

import gr.uagean.loginWebApp.service.KeyStoreService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import gr.uagean.loginWebApp.service.HttpSignatureServiceOld;

/**
 *
 * @author nikos
 */
@Service
public class HttpSignatureServiceImplLib implements HttpSignatureServiceOld {

    private final static Logger log = LoggerFactory.getLogger(HttpSignatureServiceImplLib.class);

    public static String[] requiredHeaders = {"(request-target)", "host", "original-date", "digest", "x-request-id"};

    private KeyStoreService keyServ;

    @Autowired
    public HttpSignatureServiceImplLib(KeyStoreService keyServ)
            throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try {
            this.keyServ = keyServ;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String generateSignature(String hostUrl, String method, String uri, Object postParams, String contentType, String requestId)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, UnsupportedEncodingException, IOException {
        HttpSignatureServiceImpl httpSigService;
        Key signingKey = keyServ.getSigningKey();
        String fingerPrint = "7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b";
        try {
            httpSigService = new HttpSignatureServiceImpl(fingerPrint, signingKey);
            return httpSigService.generateSignature(hostUrl, method, uri, postParams, contentType, requestId);
        } catch (InvalidKeySpecException ex) {
            log.error(ex.getMessage());
            throw new IOException("error getting the key to generate the http signature");
        }
    }

}
