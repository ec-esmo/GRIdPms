/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service;

import gr.uagean.loginWebApp.model.pojo.MSConfigurationResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

/**
 *
 * @author nikos
 */
public interface MSConfigurationService {
    
    public MSConfigurationResponse getConfigurationJSON();
    public Optional<String> getMsIDfromRSAFingerprint(String rsaFingerPrint) throws IOException;
    public Optional<PublicKey> getPublicKeyFromFingerPrint(String rsaFingerPrint) throws InvalidKeyException, IOException, NoSuchAlgorithmException, InvalidKeySpecException;

}
