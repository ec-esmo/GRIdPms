/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.factory;

import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.utils.eIDASResponseParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nikos
 */
public class AttributeSetFactory {

    public static AttributeSet make(String id, TypeEnum type, String issuer, String recipient, List<AttributeType> attributes, Map<String, String> properties) {
        AttributeType[] attrArray = new AttributeType[attributes.size()];
        return new AttributeSet(id, type, issuer, recipient, attributes.toArray(attrArray), properties, null, "low", null, null, null);
    }

    public static AttributeSet makeFromEidasResponse(String id, TypeEnum type, String issuer, String recipient, String eIDASResponse) {
        Map<String, Object> parsed = eIDASResponseParser.parseToESMOAttributeSet(eIDASResponse);
        AttributeType[] attrArray = new AttributeType[((List<AttributeType>) parsed.get(eIDASResponseParser.ATTRIBUTES_KEY)).size()];
        Map<String, String> metadataProperties = new HashMap();
        metadataProperties.put("levelOfAssurance", (String) parsed.get(eIDASResponseParser.METADATA_KEY));
        return new AttributeSet(id, type, issuer, recipient, ((List<AttributeType>) parsed.get(eIDASResponseParser.ATTRIBUTES_KEY)).toArray(attrArray),
                metadataProperties, null, "low", null, null, null);
    }

}
