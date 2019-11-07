/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

import gr.uagean.loginWebApp.model.enums.TypeEnum;
import java.util.Map;

/**
 *
 * @author nikos
 */
public class AttributeSet {

    private String id;
    private TypeEnum type;
    private String issuer;
    private String recipient;
    private AttributeType[] attributes;
    private Map<String, String> properties;

    private String inResponseTo;

    private String loa;

    private String notBefore;

    private String notAfter;

    private AttributeSetStatus status;

    public AttributeSet(String id, TypeEnum type, String issuer, String recipient, AttributeType[] attributes,
            Map<String, String> properties, String inResponseTo, String loa, String notBefore, String notAfter, AttributeSetStatus status) {
        this.id = id;
        this.type = type;
        this.issuer = issuer;
        this.recipient = recipient;
        this.attributes = attributes;
        this.properties = properties;
        this.inResponseTo = inResponseTo;
        this.loa = loa;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.status = status;
    }

    public AttributeSet() {
    }

    public String getInResponseTo() {
        return inResponseTo;
    }

    public void setInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
    }

    public String getLoa() {
        return loa;
    }

    public void setLoa(String loa) {
        this.loa = loa;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public AttributeSetStatus getStatus() {
        return status;
    }

    public void setStatus(AttributeSetStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public AttributeType[] getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeType[] attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
