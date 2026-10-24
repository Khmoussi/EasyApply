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
        name = "company_emails",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_company_emails_company_email",
                columnNames = {"company_id", "email"}
        )
)
public class CompanyEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @PrePersist
    void onCreate() {
        if (firstSeenAt == null) {
            firstSeenAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }
}
