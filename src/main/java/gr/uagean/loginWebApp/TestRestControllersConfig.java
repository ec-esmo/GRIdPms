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
import java.lang.reflect.Field;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Map;
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
        Mockito.when(paramServ().getParam("HTTPSIG_CERT_ALIAS")).thenReturn("selfsigned");
//        Mockito.when(paramServ().getParam("HTTPSIG_CERT_ALIAS")).thenReturn("1");
        Mockito.when(paramServ().getParam("ASYNC_SIGNATURE")).thenReturn("true");
//        Mockito.when(paramServ().getParam("SESSION_MANAGER_URL")).thenReturn("http://5.79.83.118:8090");
        Mockito.when(paramServ().getParam("SESSION_MANAGER_URL")).thenReturn("http://dss1.aegean.gr:8090");
//        Mockito.when(paramServ().getParam("SESSION_MANAGER_URL")).thenReturn("http://localhost:8090");
        Mockito.when(paramServ().getParam("EIDAS_PROPERTIES")).thenReturn("CurrentFamilyName,CurrentGivenName,DateOfBirth,PersonIdentifier");
        Mockito.when(paramServ().getParam("ESMO_SUPPORTED_SIG_ALGORITHMS")).thenReturn("RSA");
        Mockito.when(paramServ().getParam("ESMO_SUPPORTED_ENC_ALGORITHMS")).thenReturn("RSA");
        Mockito.when(paramServ().getParam("REDIRECT_JWT_SENDER")).thenReturn("IdPms001");
        Mockito.when(paramServ().getParam("REDIRECT_JWT_RECEIVER")).thenReturn("ACMms001");
//        Mockito.when(paramServ().getParam("CONFIGURATION_MANAGER_URL")).thenReturn("http://5.79.83.118:8080");
        Mockito.when(paramServ().getParam("CONFIGURATION_MANAGER_URL")).thenReturn("http://dss1.aegean.gr:8080");
//        Mockito.when(paramServ().getParam("SP_CONFIG_REPOSITORY")).thenReturn("/configEidas/");

        return new KeyStoreServiceImpl(paramServ());
    }

    protected static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

}
