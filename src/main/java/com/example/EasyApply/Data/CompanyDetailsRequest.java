package com.example.EasyApply.Data;

public class CompanyDetailsRequest {
    private String website;
    private String name;
    private String country;
    private String state;
    private String domain;
    /** Stable id from the LinkedIn provider (Harvest id, etc.). */
    private String id;
    private String linkedinUrl;

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

    public CompanyDetailsRequest(
            String website,
            String name,
            String country,
            String state,
            String domain,
            String id,
            String linkedinUrl
    ) {
        this.website = website;
        this.name = name;
        this.country = country;
        this.state = state;
        this.domain = domain;
        this.id = id;
        this.linkedinUrl = linkedinUrl;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }
}
