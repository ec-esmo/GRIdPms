/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service.impl;

import gr.uagean.loginWebApp.model.pojo.EndpointType;
import gr.uagean.loginWebApp.model.pojo.EntityMetadata;
import gr.uagean.loginWebApp.model.pojo.EsmoSecurityUsage;
import gr.uagean.loginWebApp.model.pojo.SecurityKeyType;
import gr.uagean.loginWebApp.service.EsmoMetadataService;
import gr.uagean.loginWebApp.service.KeyStoreService;
import gr.uagean.loginWebApp.service.ParameterService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.util.Base64;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author nikos
 */
@Service
public class EntityMetadataServiceImpl implements EsmoMetadataService {

    private final KeyStoreService keyServ;
    private final HashMap<String, String> displayNames;
    private final SecurityKeyType[] keyTypes;
    private final EndpointType[] endpoints;

    private final ParameterService paramServ;

    @Autowired
    public EntityMetadataServiceImpl(KeyStoreService keyServ, ParameterService paramServ) throws KeyStoreException, UnsupportedEncodingException {
        this.keyServ = keyServ;
        this.paramServ = paramServ;

        displayNames = new HashMap();
        displayNames.put("en", paramServ.getParam("ESMO_SERVICE_DESCRIPTION"));

        keyTypes = new SecurityKeyType[2];
        String httpSigKey = new String(keyServ.getHttpSigPublicKey().getEncoded(), StandardCharsets.UTF_8);
        SecurityKeyType httpSigKeyType = new SecurityKeyType("RSAPublicKey", EsmoSecurityUsage.signing, httpSigKey);
        keyTypes[0] = httpSigKeyType;
        if (this.keyServ.getJWTPublicKey() != null) {
            String jwtKey = new String(this.keyServ.getJWTPublicKey().getEncoded(), StandardCharsets.UTF_8);
            SecurityKeyType jwtKeyType = new SecurityKeyType("RSAPublicKey", EsmoSecurityUsage.signing, jwtKey);
            keyTypes[1] = jwtKeyType;
        }
        EndpointType endpoint = new EndpointType("POST", "POST", paramServ.getParam("ESMO_EXPOSE_URL"));
        endpoints = new EndpointType[]{endpoint};
    }

    @Override
    public EntityMetadata getMetadata() throws IOException, KeyStoreException {
        // load file from /src/resources
//        File inputFile = ResourceUtils.getFile("classpath:static/img/uaegeanI4m.png"); // new ClassPathResource("uaegeanI4m.png")
//                .getFile();
        InputStream resource = new ClassPathResource(
                "static/img/uaegeanI4m.png").getInputStream();
        byte[] fileContent = IOUtils.toByteArray(resource);//FileUtils.readFileToByteArray(inputFile);
        String encodedImage = Base64
                .getEncoder()
                .encodeToString(fileContent);
//        String encodedImage = "";
        return new EntityMetadata("https://aegean.gr/esmo/gw/idp/metadata", paramServ.getParam("ESMO_DEFAULT_NAME"), this.displayNames, encodedImage,
                new String[]{"Greece"}, "SAML", new String[]{"ACM"}, paramServ.getParam("EIDAS_PROPERTIES").split(","),
                this.endpoints, keyTypes, true, paramServ.getParam("ESMO_SUPPORTED_SIG_ALGORITHMS").split(","), true, paramServ.getParam("ESMO_SUPPORTED_ENC_ALGORITHMS").split(","), null);
    }

}
