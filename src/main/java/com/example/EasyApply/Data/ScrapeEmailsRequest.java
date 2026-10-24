package com.example.EasyApply.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrape payload: companies to crawl plus optional search context stored for DB search.
 */
public class ScrapeEmailsRequest {
    private List<String> keywords = new ArrayList<>();
    private List<String> locations = new ArrayList<>();
    private List<CompanyDetailsRequest> companies = new ArrayList<>();

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations != null ? locations : new ArrayList<>();
    }

    public List<CompanyDetailsRequest> getCompanies() {
        return companies;
    }

    public void setCompanies(List<CompanyDetailsRequest> companies) {
        this.companies = companies != null ? companies : new ArrayList<>();
    }
}
