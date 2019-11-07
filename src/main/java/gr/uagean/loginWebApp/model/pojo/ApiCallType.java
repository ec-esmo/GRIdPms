/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.loginWebApp.model.pojo;

/**
 *
 * @author nikos
 */
public enum ApiCallType {
   acmRequest,acmResponse,handleResponse,authenticate,query,
   registery,startDiscovery,process,startSession,updateSessionData,
   getSessionData,generateToken,validateToken,getSessionId
}
