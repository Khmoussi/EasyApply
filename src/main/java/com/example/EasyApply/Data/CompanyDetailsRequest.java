package com.example.EasyApply.Data;

public class CompanyDetailsRequest {
    private String website;
    private String name;
    private String country;
    private String state;
    private String domain;

    public CompanyDetailsRequest() {
    }

    public CompanyDetailsRequest(String website, String name) {
        this.website = website;
        this.name = name;
    }

    public CompanyDetailsRequest(String website, String name, String country, String state, String domain) {
        this.website = website;
        this.name = name;
        this.country = country;
        this.state = state;
        this.domain = domain;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
