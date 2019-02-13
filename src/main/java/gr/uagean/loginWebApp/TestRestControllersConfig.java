/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp;

import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.ParameterService;
import gr.uagean.loginWebApp.service.impl.KeyStoreServiceImpl;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author nikos
 */
@Profile("test")
@Configuration
public class TestRestControllersConfig {

    private ParameterService paramServ;
    private KeyStoreService keyServ;

    @Bean
    @Primary
    public ParameterService paramServ() {
        return Mockito.mock(ParameterService.class);
    }

//    @Bean
//    @Primary
//    public HttpSignatureServiceOld sigServ() throws InvalidKeySpecException, IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException {
////        MSConfigurationService msConfigServ = new MSConfigurationsServiceImplSTUB();
//        return new HttpSignatureServiceImplLib();
//    }
    @Bean
    @Primary
    public KeyStoreService keyStoreService() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testKeys/keystore.jks").getPath();
        Mockito.when(paramServ().getParam("KEYSTORE_PATH")).thenReturn(path);
        Mockito.when(paramServ().getParam("KEY_PASS")).thenReturn("selfsignedpass");
        Mockito.when(paramServ().getParam("STORE_PASS")).thenReturn("keystorepass");
        Mockito.when(paramServ().getParam("JWT_CERT_ALIAS")).thenReturn("selfsigned");
        Mockito.when(paramServ().getParam("HTTPSIG_CERT_ALIAS")).thenReturn("1");
        Mockito.when(paramServ().getParam("ASYNC_SIGNATURE")).thenReturn("true");
        Mockito.when(paramServ().getParam("SESSION_MANAGER_URL")).thenReturn("http://0.0.0.0:8090");
        Mockito.when(paramServ().getParam("EIDAS_PROPERTIES")).thenReturn("FamilyName,FirstName");
        Mockito.when(paramServ().getParam("ESMO_SUPPORTED_SIG_ALGORITHMS")).thenReturn("RSA");
        Mockito.when(paramServ().getParam("ESMO_SUPPORTED_ENC_ALGORITHMS")).thenReturn("RSA");
        return new KeyStoreServiceImpl(paramServ());
    }
}
