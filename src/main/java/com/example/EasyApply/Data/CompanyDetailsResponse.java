package com.example.EasyApply.Data;


import java.util.HashMap;
import java.util.List;

public class CompanyDetailsResponse {
    HashMap<String,String> failedMap;
    HashMap<String, CompanyDetailsRequest> successMap;

    public CompanyDetailsResponse( HashMap<String, CompanyDetailsRequest> successMap,HashMap<String, String> failedMap) {
        this.failedMap = failedMap;
        this.successMap = successMap;
    }
    CompanyDetailsResponse(){}

    public HashMap<String, String> getFailedMap() {
        return failedMap;
    }

    public void setFailedMap(HashMap<String, String> failedMap) {
        this.failedMap = failedMap;
    }

    public HashMap<String, CompanyDetailsRequest> getSuccessMap() {
        return successMap;
    }

    public void setSuccessMap(HashMap<String, CompanyDetailsRequest> successMap) {
        this.successMap = successMap;
    }
}
