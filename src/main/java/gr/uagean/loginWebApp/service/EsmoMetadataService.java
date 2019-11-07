/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.service;

import gr.uagean.loginWebApp.model.pojo.EntityMetadata;
import java.io.IOException;
import java.security.KeyStoreException;

/**
 *
 * @author nikos
 */
public interface EsmoMetadataService {
    
    public EntityMetadata getMetadata() throws IOException, KeyStoreException;

}
