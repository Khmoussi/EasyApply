package com.example.EasyApply.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "user_contacts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_contacts_user_email",
                columnNames = {"user_id", "company_email_id"}
        )
)
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_email_id", nullable = false)
    private CompanyEmail companyEmail;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @PrePersist
    void onCreate() {
        if (savedAt == null) {
            savedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public CompanyEmail getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(CompanyEmail companyEmail) {
        this.companyEmail = companyEmail;
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    public Instant getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(Instant lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}
