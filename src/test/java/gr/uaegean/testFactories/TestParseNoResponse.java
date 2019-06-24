/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.testFactories;

import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.utils.NoResponseParser;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestParseNoResponse {

    @Test
    public void test() throws IOException {

//        String response = " {\n"
//                + "    \"sub\": \"f9093e31-8592-4ebf-9925-7090579f79a0\",\n"
//                + "    \"connect-userid_sec\": [\n"
//                + "        \"eidas:$Mohamed$Al Samed$1990-08-19$SE/NO/199008199391\"\n"
//                + "    ],\n"
//                + "    \"dataporten-userid_sec\": [\n"
//                + "        \"eidas:$Mohamed$Al Samed$1990-08-19$SE/NO/199008199391\"\n"
//                + "    ],\n"
//                + "    \"name\": \"Mohamed Al Samed\",\n"
//                + "    \"picture\": \"https://api.dataporten-test.uninett.no/userinfo/v1/user/media/p:3cfe174c-b7b3-45e8-aea4-782a87aa492e\"\n"
//                + "}";
        String response = " {\n"
                + "    \"sub\": \"f9093e31-8592-4ebf-9925-7090579f79a0\",\n"
                + "    \"connect-userid_sec\": [\n"
                + "        \"eidas:SE/NO/199008199391\"\n"
                + "    ],\n"
                + "    \"dataporten-userid_sec\": [\n"
                + "        \"eidas:$Mohamed$Al Samed$1990-08-19$SE/NO/199008199391\"\n"
                + "    ],\n"
                + "    \"name\": \"Mohamed Al Samed\",\n"
                + "    \"picture\": \"https://api.dataporten-test.uninett.no/userinfo/v1/user/media/p:3cfe174c-b7b3-45e8-aea4-782a87aa492e\"\n"
                + "}";

        List<AttributeType> result = NoResponseParser.parseNoResponse(response);
        assertEquals(result.get(0).getValues()[0], "SE/NO/199008199391");
        assertEquals(result.get(0).getName(), "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier");
        assertEquals(result.get(1).getValues()[0], "Mohamed");
        assertEquals(result.get(2).getValues()[0], "Al Samed");
//        assertEquals(result.get(3).getValues()[0], "1990-08-19");
        assertEquals(result.get(3).getValues()[0], "");

    }

}
