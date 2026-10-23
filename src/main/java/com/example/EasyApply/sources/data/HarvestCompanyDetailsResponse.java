package com.example.EasyApply.sources.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Response from GET /linkedin/company
 * Docs: https://skmtc.dev/harvestapi/apis/harvestapi-linkedin-api-reference/docs/linkedin/company/get
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HarvestCompanyDetailsResponse {
    private Company element;
    private String status;
    private String error;

    public Company getElement() {
        return element;
    }

    public void setElement(Company element) {
        this.element = element;
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
    public static class Company {
        private String id;
        private String universalName;
        private String linkedinUrl;
        private String name;
        private String website;
        private String description;
        private List<CompanyLocation> locations;
        private List<Industry> industries;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUniversalName() {
            return universalName;
        }

        public void setUniversalName(String universalName) {
            this.universalName = universalName;
        }

        public String getLinkedinUrl() {
            return linkedinUrl;
        }

        public void setLinkedinUrl(String linkedinUrl) {
            this.linkedinUrl = linkedinUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<CompanyLocation> getLocations() {
            return locations;
        }

        public void setLocations(List<CompanyLocation> locations) {
            this.locations = locations;
        }

        public List<Industry> getIndustries() {
            return industries;
        }

        public void setIndustries(List<Industry> industries) {
            this.industries = industries;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyLocation {
        private String country;
        private String city;
        private String geographicArea;
        private boolean headquarter;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getGeographicArea() {
            return geographicArea;
        }

        public void setGeographicArea(String geographicArea) {
            this.geographicArea = geographicArea;
        }

        public boolean isHeadquarter() {
            return headquarter;
        }

        public void setHeadquarter(boolean headquarter) {
            this.headquarter = headquarter;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Industry {
        private String id;
        private String name;
        private String title;

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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
