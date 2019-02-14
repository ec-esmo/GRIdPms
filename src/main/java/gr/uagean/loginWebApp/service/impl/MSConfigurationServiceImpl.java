/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service.impl;

import gr.uagean.loginWebApp.model.factory.MSConfigurationResponseFactory;
import gr.uagean.loginWebApp.model.pojo.MSConfigurationResponse;
import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.MSConfigurationService;
import gr.uagean.loginWebApp.service.NetworkService;
import gr.uagean.loginWebApp.service.ParameterService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Profile("!test")
@Service
public class MSConfigurationServiceImpl implements MSConfigurationService {

    
    private final ParameterService paramServ;
    private final KeyStoreService keyServ;
    private final NetworkService netServ;
    private final HttpSignatureServiceImpl sigServ;

    //TODO cache the response for the metadata?
    private final static Logger LOG = LoggerFactory.getLogger(MSConfigurationServiceImpl.class);

    public MSConfigurationServiceImpl(@Autowired ParameterService paramServ,  @Autowired(required = false) NetworkService netServ, @Autowired KeyStoreService keyServ) throws InvalidKeySpecException, IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        this.paramServ = paramServ;

        this.keyServ = keyServ;
        Key signingKey = this.keyServ.getSigningKey();
        String fingerPrint = "7a9ba747ab5ac50e640a07d90611ce612b7bde775457f2e57b804517a87c813b";
       this.sigServ = new HttpSignatureServiceImpl(fingerPrint, signingKey);
        this.netServ = new NetworkServiceImpl(sigServ);
    }

    @Override
    public MSConfigurationResponse getConfigurationJSON() {
        try {
            String sessionMngrUrl = paramServ.getParam("CONFIGURATION_MANAGER_URL");
            List<NameValuePair> getParams = new ArrayList();
            return MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(netServ.sendGet(sessionMngrUrl, "/metadata/microservices", getParams));
        } catch (IOException | NoSuchAlgorithmException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public Optional<String> getMsIDfromRSAFingerprint(String rsaFingerPrint) throws IOException {
        Optional<MSConfigurationResponse.MicroService> msMatch = Arrays.stream(getConfigurationJSON().getMs()).filter(msConfig -> {
            return DigestUtils.sha256Hex(msConfig.getRsaPublicKeyBinary()).equals(rsaFingerPrint);
        }).findFirst();

        if (msMatch.isPresent()) {
            return Optional.of(msMatch.get().getMsId());
        }

        return Optional.empty();
    }

    @Override
    public Optional<PublicKey> getPublicKeyFromFingerPrint(String rsaFingerPrint) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Optional<MSConfigurationResponse.MicroService> msMatch = Arrays.stream(getConfigurationJSON().getMs()).filter(msConfig -> {
            return DigestUtils.sha256Hex(msConfig.getRsaPublicKeyBinary()).equals(rsaFingerPrint);
        }).findFirst();

        if (msMatch.isPresent()) {
            byte[] decoded = Base64.getDecoder().decode(msMatch.get().getRsaPublicKeyBinary());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return Optional.of(keyFactory.generatePublic(keySpec));
        }
        return Optional.empty();
    }

}
