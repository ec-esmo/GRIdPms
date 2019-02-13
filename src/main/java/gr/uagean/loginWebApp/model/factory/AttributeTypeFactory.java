/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.factory;

import gr.uagean.loginWebApp.model.pojo.AttributeType;
import java.util.List;

/**
 *
 * @author nikos
 */
public class AttributeTypeFactory {
    
    
    public static AttributeType makeAttribute(String name, String friendlyName, String encoding, String language, boolean isMandatory, List<String> values){
        String[] valArray = new String[values.size()];
        return new AttributeType(name, friendlyName, encoding, language, isMandatory, (String[])values.toArray(valArray));
    }
            
    
}
