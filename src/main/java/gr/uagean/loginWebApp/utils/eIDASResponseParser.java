/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.utils;

import gr.uagean.loginWebApp.model.pojo.AttributeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nikos
 */
public class eIDASResponseParser {

    public final static String ATTRIBUTES_KEY = "attributes";
    public final static String METADATA_KEY = "metadata";

    private final static Pattern namePattern = Pattern.compile("friendlyName='(.*?)'");
    private final static Pattern uriPattern = Pattern.compile("nameUri='(.*?)'");

    private final static Pattern valuePattern = Pattern.compile("=\\[(.*?)\\]");

    public static Map<String, String> parse(String eIDASResponse) throws IndexOutOfBoundsException {
        Map<String, String> result = new HashMap();
        String attributePart = eIDASResponse.split("attributes='")[1];
        String[] attributesStrings = attributePart.split("AttributeDefinition");

        Arrays.stream(attributesStrings).filter(string -> {
            return string.indexOf("=") > 0;
        }).filter(string -> {
            return namePattern.matcher(string).find();
        }).forEach(attrString -> {
            Matcher nameMatcher = namePattern.matcher(attrString);
            Matcher valueMatcher = valuePattern.matcher(attrString);

            if (valueMatcher.find() && nameMatcher.find()) {
                String name = nameMatcher.group(1);

                char c[] = name.toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                name = new String(c);

                String value = valueMatcher.group(1);
                result.put(name, value);
            }
        });

        return result;
    }

    public static List<AttributeType> parseToAttributeType(String eIDASRAttributesPart) throws IndexOutOfBoundsException {

        //"'AuthenticationResponse{id='_YLc6H3WhE2mjssJZHnJyOIvuRFBPIHsfszeGwVzAipyXS2csl7SlpVbKjUo4UOp', 
        //issuer='http://84.205.248.180:80/EidasNode/ConnectorResponderMetadata', status='ResponseStatus{failure='false', statusCode='urn:oasis:names:tc:SAML:2.0:status:Success', 
        //statusMessage='urn:oasis:names:tc:SAML:2.0:status:Success', subStatusCode='null'}', ipAddress='null', 
        //inResponseToId='_BJK3gNxljIfI.hOeabwBjO5ZFE54BPHQXmG9gEoXNb.BMgIN4LRZRzY18-ZyG6m', levelOfAssurance='http://eidas.europa.eu/LoA/low', 
        //attributes='{
        //AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName', 
        //friendlyName='FamilyName', personType=NaturalPerson, required=true, transliterationMandatory=true, uniqueIdentifier=false, 
        //xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentFamilyNameType', 
        //attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[cph8], 
        //AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName', 
        //friendlyName='FirstName', personType=NaturalPerson, required=true, transliterationMandatory=true, 
        //uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}CurrentGivenNameType', 
        //attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller'}=[cph8], 
        //AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/DateOfBirth', friendlyName='DateOfBirth', personType=NaturalPerson, required=true, 
        //transliterationMandatory=false, uniqueIdentifier=false, xmlType='{http://eidas.europa.eu/attributes/naturalperson}DateOfBirthType', 
        //tributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValueMarshaller'}=[1966-01-01], 
        //AttributeDefinition{nameUri='http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier', friendlyName='PersonIdentifier', personType=NaturalPerson, required=true, 
        //transliterationMandatory=false, uniqueIdentifier=true, xmlType='{http://eidas.europa.eu/attributes/naturalperson}PersonIdentifierType',
        //attributeValueMarshaller='eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller'}=[CA/CA/Cph123456]}', 
        //audienceRestriction='http://138.68.103.237:8090/metadata', notOnOrAfter='2017-09-16T08:16:21.191Z', notBefore='2017-09-16T08:11:21.191Z', country='CA', encrypted='false'}'";
        List<AttributeType> result = new ArrayList();
        String[] attributesStrings = eIDASRAttributesPart.split("AttributeDefinition");

        Arrays.stream(attributesStrings).filter(string -> {
            return string.indexOf("=") > 0;
        }).filter(string -> {
            return namePattern.matcher(string).find();
        }).forEach(attrString -> {
            Matcher nameMatcher = namePattern.matcher(attrString);
            Matcher valueMatcher = valuePattern.matcher(attrString);
            Matcher uriMatcher = uriPattern.matcher(attrString);

            if (valueMatcher.find() && nameMatcher.find() && uriMatcher.find()) {
                String friendlyName = nameMatcher.group(1);
                String name = uriMatcher.group(1);
                String value = valueMatcher.group(1);
                result.add(new AttributeType(name, friendlyName, "UTF-8", "N/A", true, new String[]{value}));
            }
        });

        return result;
    }
    
    public static String parseToMetadata(String eIDASRMetadataPart) throws IndexOutOfBoundsException {
        //"'AuthenticationResponse{id='_YLc6H3WhE2mjssJZHnJyOIvuRFBPIHsfszeGwVzAipyXS2csl7SlpVbKjUo4UOp', 
        //issuer='http://84.205.248.180:80/EidasNode/ConnectorResponderMetadata', status='ResponseStatus{failure='false', statusCode='urn:oasis:names:tc:SAML:2.0:status:Success', 
        //statusMessage='urn:oasis:names:tc:SAML:2.0:status:Success', subStatusCode='null'}', ipAddress='null', 
        //inResponseToId='_BJK3gNxljIfI.hOeabwBjO5ZFE54BPHQXmG9gEoXNb.BMgIN4LRZRzY18-ZyG6m', levelOfAssurance='http://eidas.europa.eu/LoA/low', 
        
        //audienceRestriction='http://138.68.103.237:8090/metadata', notOnOrAfter='2017-09-16T08:16:21.191Z', notBefore='2017-09-16T08:11:21.191Z', country='CA', encrypted='false'}'";
        return eIDASRMetadataPart.split("levelOfAssurance=")[1].replace(",","").replace("'", "").trim();
    }
    

    public static Map<String, Object> parseToESMOAttributeSet(String eIDASResponse) throws IndexOutOfBoundsException {
        String[] parts = eIDASResponse.split("attributes='");
        String attributePart = parts[1];
        String metdataPart = parts[0];
        List<AttributeType> attributes = parseToAttributeType(attributePart);
        Map<String,Object> result = new HashMap();
        result.put(ATTRIBUTES_KEY, attributes);
        result.put(METADATA_KEY,parseToMetadata(metdataPart));
        return result;
    }

}
