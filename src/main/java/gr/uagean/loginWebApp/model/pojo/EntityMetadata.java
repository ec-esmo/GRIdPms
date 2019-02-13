/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

import java.util.Map;

/**
 *
 * @author nikos
 */
public class EntityMetadata {
    
    
    private String entityId; //Unique identifier of the entity, usually a metadata url, e.g. "https://esmo.uji.es/gw/saml/idp/metadata.xml"
    private String defaultDisplayName;  //Name to be displayed on UI, in the default language/encoding
    private Map<String,String> displayNames; //  alternative display names, by language or encoding, e.g. {"ES" : "UJI Proveedor de Identidad","EN" : "UJI Identity Provider"}
    private String logo; // B64 string with an image binary to be displayed at UI
    private String[] location;//  nspecified list of information items about the physical or political location of the entity, to facilitate discovery
    private String protocol; // Which protocol does this entity support (SAML, OIDC, etc.)
    private String[] microservice; //list of identifiers of microservice able to handle this external entity
    private String[] claims; // list of attributes supported/requested by default by this entity, e.g. ["displayName","surname","dateOfBirth","eduPersonAffiliation"]
    private EndpointType[] endpoints; // List of service endpoints, where this Entity will accept requests/responses.
    private SecurityKeyType[] securityKeys; //List of keys held by this entity
    private boolean encryptResponses; //whether this entity will issue/expect encrypted responses
    private String[] supportedEncryptionAlg; //list of supported encryption algorithms, e.g.  ["AES256","AES512"]
    private boolean signResponses; // whether this entity will issue/expect signed responses
    private String[] supportedSigningAlg; // list of supported signing algorithms
    private Map<String,Object> otherData; //Dictionary of additional fields, specific for a certain entity type or protocol, e.g. ["attributeMappingToEIDAS" : {"displayName" : "CurrentGivenName", "surname" : "CurrentFamilyName"}]

    public EntityMetadata(String entityId, String defaultDisplayName, Map<String, String> displayNames, String logo, String[] location, String protocol, String[] microservice, String[] claims, EndpointType[] endpoints, SecurityKeyType[] securityKeys, boolean encryptResponses, String[] supportedEncryptionAlg, boolean signResponses, String[] supportedSigningAlg, Map<String, Object> otherData) {
        this.entityId = entityId;
        this.defaultDisplayName = defaultDisplayName;
        this.displayNames = displayNames;
        this.logo = logo;
        this.location = location;
        this.protocol = protocol;
        this.microservice = microservice;
        this.claims = claims;
        this.endpoints = endpoints;
        this.securityKeys = securityKeys;
        this.encryptResponses = encryptResponses;
        this.supportedEncryptionAlg = supportedEncryptionAlg;
        this.signResponses = signResponses;
        this.supportedSigningAlg = supportedSigningAlg;
        this.otherData = otherData;
    }

    public EntityMetadata() {
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    public void setDefaultDisplayName(String defaultDisplayName) {
        this.defaultDisplayName = defaultDisplayName;
    }

    public Map<String, String> getDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(Map<String, String> displayNames) {
        this.displayNames = displayNames;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String[] getMicroservice() {
        return microservice;
    }

    public void setMicroservice(String[] microservice) {
        this.microservice = microservice;
    }

    public String[] getClaims() {
        return claims;
    }

    public void setClaims(String[] claims) {
        this.claims = claims;
    }

    public EndpointType[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(EndpointType[] endpoints) {
        this.endpoints = endpoints;
    }

    public SecurityKeyType[] getSecurityKeys() {
        return securityKeys;
    }

    public void setSecurityKeys(SecurityKeyType[] securityKeys) {
        this.securityKeys = securityKeys;
    }

    public boolean isEncryptResponses() {
        return encryptResponses;
    }

    public void setEncryptResponses(boolean encryptResponses) {
        this.encryptResponses = encryptResponses;
    }

    public String[] getSupportedEncryptionAlg() {
        return supportedEncryptionAlg;
    }

    public void setSupportedEncryptionAlg(String[] supportedEncryptionAlg) {
        this.supportedEncryptionAlg = supportedEncryptionAlg;
    }

    public boolean isSignResponses() {
        return signResponses;
    }

    public void setSignResponses(boolean signResponses) {
        this.signResponses = signResponses;
    }

    public String[] getSupportedSigningAlg() {
        return supportedSigningAlg;
    }

    public void setSupportedSigningAlg(String[] supportedSigningAlg) {
        this.supportedSigningAlg = supportedSigningAlg;
    }

    public Map<String, Object> getOtherData() {
        return otherData;
    }

    public void setOtherData(Map<String, Object> otherData) {
        this.otherData = otherData;
    }


    
    
    
    
    
    
    
}
