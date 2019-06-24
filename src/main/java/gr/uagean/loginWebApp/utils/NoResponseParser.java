/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.model.pojo.NoPerson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nikos
 */
public class NoResponseParser {

    private final static Logger log = LoggerFactory.getLogger(NoResponseParser.class);

    public static List<AttributeType> parseNoResponse(String noResponse) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        NoPerson person = mapper.readValue(noResponse, NoPerson.class);

        log.info("Response from Norway::" + noResponse);

        List<AttributeType> parsed = new ArrayList();
//        String[] eidasAttributes = person.getConnectUserIdSec()[0].split(Pattern.quote("$"));
//        String eidasIdentifier = eidasAttributes[4];
//        String givenName = eidasAttributes[1];
//        String familyName = eidasAttributes[2];
//        String dateOfBirth = eidasAttributes[3];

        String eidasIdentifier;
        String givenName;
        String familyName;
        String dateOfBirth = "";
        if (person.getUser() == null) {
            eidasIdentifier = person.getConnectUserIdSec()[0].split("eidas:")[1].trim();
            givenName = person.getName().split(" ")[0].trim();
            familyName = person.getName().replace(givenName, "").trim();
            dateOfBirth = "";

        } else {
            eidasIdentifier = person.getUser().getConnectUserIdSec()[0].split("eidas:")[1].trim();
            givenName = person.getUser().getName().split(" ")[0].trim();
            familyName = person.getUser().getName().replace(givenName, "").trim();
            dateOfBirth = "";

        }

        AttributeType eidasIdentifierType = new AttributeType("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "PersonIdentifier", "UTF-8", "N/A", true, new String[]{eidasIdentifier});
        AttributeType givenNameType = new AttributeType("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "FirstName", "UTF-8", "N/A", true, new String[]{givenName});
        AttributeType familyNameType = new AttributeType("http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "FamilyName", "UTF-8", "N/A", true, new String[]{familyName});
        AttributeType dateOfBirthType = new AttributeType("http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "DateOfBirth", "UTF-8", "N/A", true, new String[]{dateOfBirth});

        parsed.add(eidasIdentifierType);
        parsed.add(givenNameType);
        parsed.add(familyNameType);
        parsed.add(dateOfBirthType);
        return parsed;

    }

}
