/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service.impl;

 
 
import gr.uagean.loginWebApp.model.factory.MSConfigurationResponseFactory;
import gr.uagean.loginWebApp.model.pojo.MSConfigurationResponse;
import gr.uagean.loginWebApp.model.pojo.MSConfigurationResponse.MicroService;
import gr.uagean.loginWebApp.service.MSConfigurationService;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Profile("test")
@Service
public class MSConfigurationsServiceImplSTUB implements MSConfigurationService {

    private final static Logger log = LoggerFactory.getLogger(MSConfigurationsServiceImplSTUB.class);

    @Override
    public MSConfigurationResponse getConfigurationJSON() {

        try {
            return MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile("configurationResponse.json"));
        } catch (IOException e) {
            log.error("file not found ", e);
            return null;
        }

    }

    private String getFile(String fileName) {
        StringBuilder result = new StringBuilder("");
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();

    }

    @Override
    public Optional<String> getMsIDfromRSAFingerprint(String rsaFingerPrint) throws IOException {
        MSConfigurationResponse configResp = MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile("configurationResponse.json"));
        Optional<MicroService> msMatch = Arrays.stream(configResp.getMs()).filter(msConfig -> {
            return DigestUtils.sha256Hex(msConfig.getRsaPublicKeyBinary()).equals(rsaFingerPrint);
        }).findFirst();

        if (msMatch.isPresent()) {
            return Optional.of(msMatch.get().getMsId());
        }

        return Optional.empty();
    }

    @Override
    public Optional<PublicKey> getPublicKeyFromFingerPrint(String rsaFingerPrint) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        MSConfigurationResponse configResp = MSConfigurationResponseFactory.makeMSConfigResponseFromJSON(getFile("configurationResponse.json"));
        Optional<MicroService> msMatch = Arrays.stream(configResp.getMs()).filter(msConfig -> {
            return DigestUtils.sha256Hex(msConfig.getRsaPublicKeyBinary()).equals(rsaFingerPrint);
        }).findFirst();

        if (msMatch.isPresent()) {
            //Base64.getEncoder().encodeToString(key.getEncoded())
            byte[] decoded = Base64.getDecoder().decode(msMatch.get().getRsaPublicKeyBinary());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return Optional.of(keyFactory.generatePublic(keySpec));
        }
        return Optional.empty();
    }

}
