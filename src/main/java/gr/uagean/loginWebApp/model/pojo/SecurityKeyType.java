/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

/**
 *
 * @author nikos
 */
public class SecurityKeyType {
    private String KeyType; // String identifying the kind of key e.g. "RSAPublicKey"
    private EsmoSecurityUsage usage; // To which use is this key intended.
    private String key ; // B64 string representing the key binary 

    public SecurityKeyType() {
    }

    public SecurityKeyType(String KeyType, EsmoSecurityUsage usage, String key) {
        this.KeyType = KeyType;
        this.usage = usage;
        this.key = key;
    }
    
    

    public String getKeyType() {
        return KeyType;
    }

    public void setKeyType(String KeyType) {
        this.KeyType = KeyType;
    }

    public EsmoSecurityUsage getUsage() {
        return usage;
    }

    public void setUsage(EsmoSecurityUsage usage) {
        this.usage = usage;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    

            
    
            
 }
