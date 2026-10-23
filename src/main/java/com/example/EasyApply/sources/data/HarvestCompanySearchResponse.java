package com.example.EasyApply.sources.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HarvestCompanySearchResponse {
    private List<HarvestCompanyShort> elements;
    private String status;
    private String error;

    public List<HarvestCompanyShort> getElements() {
        return elements;
    }

    public void setElements(List<HarvestCompanyShort> elements) {
        this.elements = elements;
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
