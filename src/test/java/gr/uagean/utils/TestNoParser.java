/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.utils;

import gr.uagean.loginWebApp.model.pojo.AttributeType;
import gr.uagean.loginWebApp.utils.NoResponseParser;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author nikos
 */
public class TestNoParser {

    @Test
    public void testNoParseProduction() throws IOException {

        String response = "{\n"
                + "    \"user\": {\n"
                + "        \"userid_sec\": [\n"
                + "            \"eidas:EE/NO/49311086012\"\n"
                + "        ],\n"
                + "        \"userid\": \"e91a6752-999e-4f98-938d-1b2a34891173\",\n"
                + "        \"name\": \"TRIIN PUUSAAR\",\n"
                + "        \"profilephoto\": \"p:c048174c-056b-432c-adb8-2b0c5cff1fc9\"\n"
                + "    },\n"
                + "    \"audience\": \"843a3f7b-1575-467f-a696-00efff753076\"\n"
                + "}";

        List<AttributeType> attr = NoResponseParser.parseNoResponse(response);

    }

    @Test
    public void testNoParsePreProduction() throws IOException {

        String response = "{\n"
                + "    \"sub\": \"6f71b1d9-8f43-4761-9ad0-43976c3f602c\",\n"
                + "    \"connect-userid_sec\": [\n"
                + "        \"eidas:SE/NO/199008199391\"\n"
                + "    ],\n"
                + "    \"dataporten-userid_sec\": [\n"
                + "        \"eidas:SE/NO/199008199391\"\n"
                + "    ],\n"
                + "    \"name\": \"Mohamed Al Samed\",\n"
                + "    \"picture\": \"https://api.dataporten-test.uninett.no/userinfo/v1/user/media/p:442f87ff-7311-448f-85ce-fb55aff78120\"\n"
                + "}";

        List<AttributeType> attr = NoResponseParser.parseNoResponse(response);

    }

}
