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
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 *
 * @author nikos
 */
public class MSConfigurationResponse {

    private MicroService[] ms;

    public MSConfigurationResponse() {
    }

    public MSConfigurationResponse(MicroService[] ms) {
        this.ms = ms;
    }

    public MicroService[] getMs() {
        return ms;
    }

    public void setMs(MicroService[] ms) {
        this.ms = ms;
    }

    //static is needed for jackson
    public static class MicroService {

        @JsonAlias({"msId", "msID"})
        private String msId;
        private String[] authorizedMicroservices; // List of ms identifiers that will be authorised to contact this microservice (will be used by the SM when validating a token).
        private String msType;
        private String rsaPublicKeyBinary;
        private PublishedAPI[] publishedAPI;

        public MicroService(String msID, String msType, String rsaPublicKeyBinary, PublishedAPI[] publishedAPI, String[] authorizedMicroservices) {
            this.msId = msID;
            this.msType = msType;
            this.rsaPublicKeyBinary = rsaPublicKeyBinary;
            this.publishedAPI = publishedAPI;
            this.authorizedMicroservices= authorizedMicroservices;
        }

        public String[] getAuthorizedMicroservices() {
            return authorizedMicroservices;
        }

        public void setAuthorizedMicroservices(String[] authorizedMicroservices) {
            this.authorizedMicroservices = authorizedMicroservices;
        }

        
        public MicroService() {
        }

        public String getMsId() {
            return msId;
        }

        public void setMsId(String msId) {
            this.msId = msId;
        }

        public String getMsType() {
            return msType;
        }

        public void setMsType(String msType) {
            this.msType = msType;
        }

        public String getRsaPublicKeyBinary() {
            return rsaPublicKeyBinary;
        }

        public void setRsaPublicKeyBinary(String rsaPublicKeyBinary) {
            this.rsaPublicKeyBinary = rsaPublicKeyBinary;
        }

        public PublishedAPI[] getPublishedAPI() {
            return publishedAPI;
        }

        public void setPublishedAPI(PublishedAPI[] publishedAPI) {
            this.publishedAPI = publishedAPI;
        }

    }

    //static is needed for jackson
    public static class PublishedAPI {

        private ApiClassEnum apiClass;
        private ApiCallType apiCall;
        private ApiConnectionType apiConnectionType;
        private String apiEndpoint;

        public PublishedAPI() {
        }

        public PublishedAPI(ApiClassEnum apiClass, ApiCallType apiCall, ApiConnectionType apiConnectionType, String url) {
            this.apiClass = apiClass;
            this.apiCall = apiCall;
            this.apiConnectionType = apiConnectionType;
            this.apiEndpoint = url;
        }

        public ApiClassEnum getApiClass() {
            return apiClass;
        }

        public void setApiClass(ApiClassEnum apiClass) {
            this.apiClass = apiClass;
        }

        public ApiCallType getApiCall() {
            return apiCall;
        }

        public void setApiCall(ApiCallType apiCall) {
            this.apiCall = apiCall;
        }

        public ApiConnectionType getApiConnectionType() {
            return apiConnectionType;
        }

        public void setApiConnectionType(ApiConnectionType apiConnectionType) {
            this.apiConnectionType = apiConnectionType;
        }

        public String getApiEndpoint() {
            return apiEndpoint;
        }

        public void setApiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
        }

    }

}
