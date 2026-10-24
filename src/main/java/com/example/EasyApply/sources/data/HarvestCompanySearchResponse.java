package com.example.EasyApply.sources.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HarvestCompanySearchResponse {
    private List<HarvestCompanyShort> elements;
    private Pagination pagination;
    private String status;
    private String error;

    public List<HarvestCompanyShort> getElements() {
        return elements;
    }

    public void setElements(List<HarvestCompanyShort> elements) {
        this.elements = elements;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pagination {
        private int totalPages;
        private int totalElements;
        private int pageNumber;
        private int previousElements;
        private int pageSize;
        private String paginationToken;

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(int totalElements) {
            this.totalElements = totalElements;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPreviousElements() {
            return previousElements;
        }

        public void setPreviousElements(int previousElements) {
            this.previousElements = previousElements;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public String getPaginationToken() {
            return paginationToken;
        }

        public void setPaginationToken(String paginationToken) {
            this.paginationToken = paginationToken;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HarvestCompanyShort {
        private String id;
        private String name;
        private String industries;
        private Location location;
        private String followers;
        private String summary;
        private String logo;
        private String linkedinUrl;
        private String universalName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIndustries() {
            return industries;
        }

        public void setIndustries(String industries) {
            this.industries = industries;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public String getFollowers() {
            return followers;
        }

        public void setFollowers(String followers) {
            this.followers = followers;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getLinkedinUrl() {
            return linkedinUrl;
        }

        public void setLinkedinUrl(String linkedinUrl) {
            this.linkedinUrl = linkedinUrl;
        }

        public String getUniversalName() {
            return universalName;
        }

        public void setUniversalName(String universalName) {
            this.universalName = universalName;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String linkedinText;

        public String getLinkedinText() {
            return linkedinText;
        }

        public void setLinkedinText(String linkedinText) {
            this.linkedinText = linkedinText;
        }
    }
}
