/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.testFactories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.enums.TypeEnum;
import gr.uagean.loginWebApp.model.factory.AttributeSetFactory;
import gr.uagean.loginWebApp.model.factory.AttributeTypeFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeSet;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
public class testAttributeSetFactory {

    @Test
    public void testAttributeFactory() throws JsonProcessingException {
        ArrayList<String> values = new ArrayList();
        values.add("value1");
        values.add("value2");
        AttributeType attr = AttributeTypeFactory.makeAttribute("name", "friendly", "utf-8", "gr GR", true, values);

        ArrayList<String> values2 = new ArrayList();
        values2.add("value12");
        values2.add("value22");
        AttributeType attr2 = AttributeTypeFactory.makeAttribute("name", "friendly", "utf-8", "gr GR", true, values2);

        ArrayList<AttributeType> attributes = new ArrayList();
        attributes.add(attr);
        attributes.add(attr2);

        AttributeSet attrSet = AttributeSetFactory.make("testId", TypeEnum.Request, "issuer", "recipient", attributes, new HashMap());
        assertEquals(attrSet.getAttributes()[0].getValues()[0],"value1");
        assertEquals(attrSet.getAttributes()[1].getValues()[0],"value12");
        
        
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(attrSet));
        assertEquals(mapper.writeValueAsString(attrSet),"{\"id\":\"testId\",\"type\":\"Request\",\"issuer\":\"issuer\",\"recipient\":\"recipient\",\"attributes\":[{\"name\":\"name\",\"friendlyName\":\"friendly\",\"encoding\":\"utf-8\",\"language\":\"gr GR\",\"isMandatory\":true,\"values\":[\"value1\",\"value2\"]},{\"name\":\"name\",\"friendlyName\":\"friendly\",\"encoding\":\"utf-8\",\"language\":\"gr GR\",\"isMandatory\":true,\"values\":[\"value12\",\"value22\"]}],\"properties\":{}}");

    }

}
