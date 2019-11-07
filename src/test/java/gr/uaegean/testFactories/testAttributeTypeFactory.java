/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.testFactories;

import gr.uagean.loginWebApp.model.factory.AttributeTypeFactory;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author nikos
 */
@ActiveProfiles("test")
public class testAttributeTypeFactory {
    
    
    @Test
    public void testAttributeFactory(){
        ArrayList<String> values = new ArrayList();
        values.add("value1");
        values.add("value2");
        AttributeType attr = AttributeTypeFactory.makeAttribute("name", "friendly", "utf-8", "gr GR", true, values);
        assertEquals(attr.getValues()[0],"value1");
        assertEquals(attr.getValues()[1],"value2");
    
    
    }
    
}
