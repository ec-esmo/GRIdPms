/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.factory;

import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeSetStatus;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.utils.eIDASResponseParser;
import static gr.uagean.loginWebApp.utils.eIDASResponseParser.NAME_ID_KEY;
import gr.uagean.loginWebApp.utils.NoResponseParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nikos
 */
public class AttributeSetFactory {

    public static String NameID = "NameID";

    public static AttributeSet make(String id, TypeEnum type, String issuer, String recipient, List<AttributeType> attributes, Map<String, String> properties) {
        AttributeType[] attrArray = new AttributeType[attributes.size()];
        return new AttributeSet(id, type, issuer, recipient, attributes.toArray(attrArray), properties, null, "low", null, null, null);
    }

    public static AttributeSet makeFromEidasResponse(String id, TypeEnum type, String issuer, String recipient, String eIDASResponse) {
        Map<String, Object> parsed = eIDASResponseParser.parseToESMOAttributeSet(eIDASResponse);
        AttributeType[] attrArray = new AttributeType[((List<AttributeType>) parsed.get(eIDASResponseParser.ATTRIBUTES_KEY)).size()];

        Map<String, String> metadataProperties = new HashMap();
        metadataProperties.put("levelOfAssurance", (String) parsed.get(eIDASResponseParser.METADATA_KEY));
        metadataProperties.put("NameID", (String) parsed.get(NAME_ID_KEY));

        AttributeSetStatus atrSetStatus = new AttributeSetStatus();
        atrSetStatus.setCode(AttributeSetStatus.CodeEnum.OK);

        return new AttributeSet(id, type, issuer, recipient, ((List<AttributeType>) parsed.get(eIDASResponseParser.ATTRIBUTES_KEY)).toArray(attrArray),
                metadataProperties, null, "low", null, null, atrSetStatus);
    }

    public static AttributeSet makeFromNoResponse(String id, TypeEnum type, String issuer, String recipient, String noResponse) throws IOException {
        List<AttributeType> attributes = NoResponseParser.parseNoResponse(noResponse);
        AttributeType[] attrArray = new AttributeType[attributes.size()];

        AttributeSetStatus atrSetStatus = new AttributeSetStatus();
        atrSetStatus.setCode(AttributeSetStatus.CodeEnum.OK);

        Map<String, String> metadataProperties = new HashMap();
        metadataProperties.put("levelOfAssurance", "low");
        metadataProperties.put("NameID", attributes.get(0).getValues()[0]);

        return new AttributeSet(id, type, issuer, recipient, attributes.toArray(attrArray),
                metadataProperties, null, "low", null, null, atrSetStatus);
    }

}
