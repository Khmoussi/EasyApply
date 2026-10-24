package com.example.EasyApply.billing;

/**
 * Plan limits (companies + sends only). Scrapes / directory are not metered.
 */
public enum PlanType {
    TRIAL(30, 30, 10),
    STARTER(200, 200, 50),
    PRO(1000, 1000, 100);

    private final int companiesPerMonth;
    private final int sendsPerMonth;
    private final int maxSearchLimit;

    PlanType(int companiesPerMonth, int sendsPerMonth, int maxSearchLimit) {
        this.companiesPerMonth = companiesPerMonth;
        this.sendsPerMonth = sendsPerMonth;
        this.maxSearchLimit = maxSearchLimit;
    }

    public int getCompaniesPerMonth() {
        return companiesPerMonth;
    }

    public int getSendsPerMonth() {
        return sendsPerMonth;
    }

    public int getMaxSearchLimit() {
        return maxSearchLimit;
    }
}
