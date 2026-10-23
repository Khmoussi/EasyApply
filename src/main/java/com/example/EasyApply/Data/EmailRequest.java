package com.example.EasyApply.Data;

import java.util.ArrayList;

// EmailRequest class for request payload
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private String attachmentPath = "";
    private ArrayList<CustomEmail> emailList;
    private String senderName;

    public EmailRequest() {
    }

    public EmailRequest(String to, String subject, String body, String attachmentPath, String senderName) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachmentPath = attachmentPath;
        this.senderName = senderName;
    }

    // Getters and Setters
    public String getTo() {
        return to;
    }

    public ArrayList<CustomEmail> getEmailList() {
        return emailList;
    }

    public void setEmailList(ArrayList<CustomEmail> emailList) {
        this.emailList = emailList;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
