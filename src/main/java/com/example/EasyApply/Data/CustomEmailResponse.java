package com.example.EasyApply.Data;

import java.util.ArrayList;
import java.util.List;

public class CustomEmailResponse {
    private boolean status;
    private List<CustomEmail> sent;
    private List<CustomEmail> failed;

    public CustomEmailResponse() {
        this.status = true;
        this.sent = new ArrayList<>();
        this.failed = new ArrayList<>();
    }

    public CustomEmailResponse(boolean status, List<CustomEmail> sent, List<CustomEmail> failed) {
        this.status = status;
        this.sent = sent != null ? sent : new ArrayList<>();
        this.failed = failed != null ? failed : new ArrayList<>();
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<CustomEmail> getSent() {
        return sent;
    }

    public void setSent(List<CustomEmail> sent) {
        this.sent = sent;
    }

    public List<CustomEmail> getFailed() {
        return failed;
    }

    public void setFailed(List<CustomEmail> failed) {
        this.failed = failed;
    }

    /** @deprecated use {@link #getFailed()} */
    public List<CustomEmail> getEmails() {
        return failed;
    }
}
