/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service.impl;


import gr.uagean.loginWebApp.model.factory.MSConfigurationResponseFactory;
import gr.uagean.loginWebApp.model.pojo.MSConfigurationResponse;
import gr.uagean.loginWebApp.service.MSConfigurationService;
import gr.uagean.loginWebApp.service.ParameterService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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
import gr.uagean.loginWebApp.service.NetworkServiceOld;

/**
 *
 * @author nikos
 */
@Profile("!test")
@Service
public class MSConfigurationServiceImpl implements MSConfigurationService {

    private final ParameterService paramServ;
    private final NetworkServiceOld netServ;

    //TODO cache the response for the metadata?
    private final static Logger LOG = LoggerFactory.getLogger(MSConfigurationServiceImpl.class);

    @Autowired
    public MSConfigurationServiceImpl(ParameterService paramServ, NetworkServiceOld netServ) {
        this.paramServ = paramServ;
        this.netServ = netServ;
    }

    @Override
    public MSConfigurationResponse getConfigurationJSON() {
        try {
            String sessionMngrUrl = paramServ.getParam("CONFIGURATION_MANAGER_URL");
            List<NameValuePair> getParams = new ArrayList();
            return MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(netServ.sendGetStringResponse(sessionMngrUrl, "/metadata/microservices", getParams));
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
