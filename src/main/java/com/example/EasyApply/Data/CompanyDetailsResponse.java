package com.example.EasyApply.Data;


import java.util.HashMap;
import java.util.List;

public class CompanyDetailsResponse {
    HashMap<String,String> failedMap;
    HashMap<String, String> successMap;

    public CompanyDetailsResponse( HashMap<String, String> successMap,HashMap<String, String> failedMap) {
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

    public HashMap<String, String> getSuccessMap() {
        return successMap;
    }

    public void setSuccessMap(HashMap<String, String> successMap) {
        this.successMap = successMap;
    }
}
