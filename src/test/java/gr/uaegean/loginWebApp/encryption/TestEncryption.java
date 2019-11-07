/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.loginWebApp.encryption;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import realAES.realAES;

/**
 *
 * @author nikos
 */
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application.properties")
public class TestEncryption {

    @Autowired
    private Environment env;

    @Test
    public void test() {
        realAES test = new realAES("stou apostoli to koutouki", "SieBcx3RlfgJ3b5e5SkZTrHPkKDFEfYSsJ/N1UbCtFU=", "encrypt");
        test.setEncrypted(test.aesEncrypt());
        System.out.println(test.getEncrypted());
    }

    @Test
    public void test2() {
//        String ecryptedKey = env.getProperty("encrypted.key");
        realAES test = new realAES();
        String ecryptedKey = env.getProperty("encrypted.key");
        System.out.println(ecryptedKey);
        String decryptedKey = test.aesDecrypt(ecryptedKey, "SieBcx3RlfgJ3b5e5SkZTrHPkKDFEfYSsJ/N1UbCtFU=");
        System.out.println(decryptedKey);
    }

}
