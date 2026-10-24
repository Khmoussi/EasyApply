package com.example.EasyApply.billing;

import java.time.Instant;

public class UsageDto {
    private String plan;
    private String status;
    private Instant trialEndsAt;
    private Instant periodStart;
    private Instant periodEnd;
    private int companiesUsed;
    private int companiesLimit;
    private int companiesRemaining;
    private int sendsUsed;
    private int sendsLimit;
    private int sendsRemaining;
    private int maxSearchLimit;
    private boolean canUseProduct;

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTrialEndsAt() {
        return trialEndsAt;
    }

    public void setTrialEndsAt(Instant trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    public int getCompaniesUsed() {
        return companiesUsed;
    }

    public void setCompaniesUsed(int companiesUsed) {
        this.companiesUsed = companiesUsed;
    }

    public int getCompaniesLimit() {
        return companiesLimit;
    }

    public void setCompaniesLimit(int companiesLimit) {
        this.companiesLimit = companiesLimit;
    }

    public int getCompaniesRemaining() {
        return companiesRemaining;
    }

    public void setCompaniesRemaining(int companiesRemaining) {
        this.companiesRemaining = companiesRemaining;
    }

    public int getSendsUsed() {
        return sendsUsed;
    }

    public void setSendsUsed(int sendsUsed) {
        this.sendsUsed = sendsUsed;
    }

    public int getSendsLimit() {
        return sendsLimit;
    }

    public void setSendsLimit(int sendsLimit) {
        this.sendsLimit = sendsLimit;
    }

    public int getSendsRemaining() {
        return sendsRemaining;
    }

    public void setSendsRemaining(int sendsRemaining) {
        this.sendsRemaining = sendsRemaining;
    }

    public int getMaxSearchLimit() {
        return maxSearchLimit;
    }

    public void setMaxSearchLimit(int maxSearchLimit) {
        this.maxSearchLimit = maxSearchLimit;
    }

    public boolean isCanUseProduct() {
        return canUseProduct;
    }

    public void setCanUseProduct(boolean canUseProduct) {
        this.canUseProduct = canUseProduct;
    }
}
