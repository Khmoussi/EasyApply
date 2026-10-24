package com.example.EasyApply.billing;

public class QuotaExceededException extends RuntimeException {

    private final String code;
    private final String metric;
    private final int used;
    private final int limit;
    private final PlanType plan;

    public QuotaExceededException(String code, String metric, int used, int limit, PlanType plan) {
        super(code + ": " + metric + " " + used + "/" + limit);
        this.code = code;
        this.metric = metric;
        this.used = used;
        this.limit = limit;
        this.plan = plan;
    }

    public String getCode() {
        return code;
    }

    public String getMetric() {
        return metric;
    }

    public int getUsed() {
        return used;
    }

    public int getLimit() {
        return limit;
    }

    public PlanType getPlan() {
        return plan;
    }
}
