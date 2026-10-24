package com.example.EasyApply.sources.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Company search request shared by Apify and Harvest providers.
 */
public class LinkedInRequest {
    private String action = "get-companies";
    private int limit;
    /** 1-based page for Harvest company-search. Ignored by Apify. */
    private int page = 1;
    private List<String> location;
    private List<String> keywords;
    /**
     * Already-seen Harvest / LinkedIn company ids from prior pages in this session.
     * Skipped before expensive company-detail enrichment.
     */
    private List<String> excludeIds = new ArrayList<>();
    /** Already-seen LinkedIn profile URLs from prior pages. */
    private List<String> excludeLinkedInUrls = new ArrayList<>();

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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
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

    public List<String> getExcludeIds() {
        return excludeIds;
    }

    public void setExcludeIds(List<String> excludeIds) {
        this.excludeIds = excludeIds != null ? excludeIds : new ArrayList<>();
    }

    public List<String> getExcludeLinkedInUrls() {
        return excludeLinkedInUrls;
    }

    public void setExcludeLinkedInUrls(List<String> excludeLinkedInUrls) {
        this.excludeLinkedInUrls = excludeLinkedInUrls != null ? excludeLinkedInUrls : new ArrayList<>();
    }
}
