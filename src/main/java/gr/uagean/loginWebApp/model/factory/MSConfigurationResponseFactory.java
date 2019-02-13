/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.factory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.loginWebApp.model.pojo.MSConfigurationResponse;
import java.io.IOException;

/**
 *
 * @author nikos
 */
public class MSConfigurationResponseFactory {

    public static MSConfigurationResponse makeMSConfigResponseFromJSON(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, MSConfigurationResponse.class);
    }

}
