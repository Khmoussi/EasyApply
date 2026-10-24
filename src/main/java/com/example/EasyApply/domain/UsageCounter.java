package com.example.EasyApply.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "usage_counters",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_usage_counters_user_period",
                columnNames = {"user_id", "period_start"}
        )
)
public class UsageCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "companies_used", nullable = false)
    private int companiesUsed;

    @Column(name = "sends_used", nullable = false)
    private int sendsUsed;

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    public int getCompaniesUsed() {
        return companiesUsed;
    }

    public void setCompaniesUsed(int companiesUsed) {
        this.companiesUsed = companiesUsed;
    }

    public int getSendsUsed() {
        return sendsUsed;
    }

    public void setSendsUsed(int sendsUsed) {
        this.sendsUsed = sendsUsed;
    }
}
