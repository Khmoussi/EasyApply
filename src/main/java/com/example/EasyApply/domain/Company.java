package com.example.EasyApply.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = @UniqueConstraint(name = "uk_companies_website_key", columnNames = "website_key")
)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Normalized website used for dedupe (lowercase, no trailing slash). */
    @Column(name = "website_key", nullable = false, length = 512)
    private String websiteKey;

    @Column(nullable = false, length = 512)
    private String website;

    @Column(nullable = false, length = 512)
    private String name;

    @Column(length = 255)
    private String country;

    @Column(length = 255)
    private String state;

    @Column(length = 255)
    private String domain;

    @Column(name = "linkedin_url", length = 1024)
    private String linkedinUrl;

    @Column(name = "provider_id", length = 128)
    private String providerId;

    /** Space-joined search keywords captured at scrape time. */
    @Column(length = 2000)
    private String keywords;

    /** Space-joined location filters captured at scrape time. */
    @Column(name = "location_text", length = 1000)
    private String locationText;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getWebsiteKey() {
        return websiteKey;
    }

    public void setWebsiteKey(String websiteKey) {
        this.websiteKey = websiteKey;
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

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
