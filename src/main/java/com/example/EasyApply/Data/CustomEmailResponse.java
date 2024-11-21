package com.example.EasyApply.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomEmailResponse {
    boolean status;
    List<CustomEmail> emails;

    public CustomEmailResponse(AtomicBoolean status, List<CustomEmail> emails) {
        this.status = status.get();
        this.emails = emails;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<CustomEmail> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<CustomEmail> emails) {
        this.emails = emails;
    }
}
