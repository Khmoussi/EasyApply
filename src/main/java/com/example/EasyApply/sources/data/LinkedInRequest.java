package com.example.EasyApply.sources.data;

import java.util.List;

/**
 * Company search request shared by Apify and Harvest providers.
 */
public class LinkedInRequest {
    private String action = "get-companies";
    private int limit;
    private List<String> location;
    private List<String> keywords;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<String> getLocation() {
        return location;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
