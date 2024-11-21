package com.example.EasyApply.Controllers;

import com.example.EasyApply.Data.CustomEmail;
import com.example.EasyApply.Data.CustomEmailResponse;
import com.example.EasyApply.Services.GmailService;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class EmailController {

    @Autowired
    private GmailService gmailService;
    @GetMapping("/ss")
    public String a(){
        return "ss";
    }

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestBody EmailRequest emailRequest,  @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract the OAuth2 access token from the Authorization header
            String accessToken = authorizationHeader.replace("Bearer ", "");

            // Create a Credential object using the access token
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

                       gmailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody(), credential);
            return "Email sent successfully!";
        } catch (MessagingException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return "Failed to send email: " + e.getMessage();
        }
    }

    @PostMapping("/sendEmailWithAttachment")
    public String sendEmailWithAttachment(@RequestBody EmailRequest emailRequest, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract the OAuth2 access token from the Authorization header
            String accessToken = authorizationHeader.replace("Bearer ", "");

            // Create a Credential object using the access token
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

            Path attachmentPath = Paths.get(emailRequest.getAttachmentPath());
            gmailService.sendEmailWithAttachment(
                    emailRequest.getTo(),
                    emailRequest.getSubject(),
                    emailRequest.getBody(),
                    attachmentPath,
                    credential
            );
            return "Email with attachment sent successfully!";
        } catch (MessagingException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return "Failed to send email with attachment: " + e.getMessage();
        }
    }

    @PostMapping("/sendAutomatedEmailsWithAttachment")
    public ResponseEntity<?> sendAutomatedEmailsWithAttachment(@RequestBody EmailRequest emailRequest, @RequestHeader("Authorization") String authorizationHeader) {

            // Extract the OAuth2 access token from the Authorization header
            String accessToken = authorizationHeader.replace("Bearer ", "");

            // Create a Credential object using the access token
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

            Path attachmentPath = Paths.get(emailRequest.getAttachmentPath());
          CustomEmailResponse result=  gmailService.sendAutomatedEmails(

                    emailRequest.getSubject(),
                    emailRequest.getBody(),
                    attachmentPath,
                    credential,
                    emailRequest.getEmailList(),
                  emailRequest.getSenderName()
            );
          if(result.isStatus()){
              if(result.getEmails().isEmpty())
                  return ResponseEntity.ok("Email with attachment sent successfully!");
              return ResponseEntity.ok(result);
          }else{
              return ResponseEntity.badRequest().body("Token invalid ");
          }


    }

}

// EmailRequest class for request payload
class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private String attachmentPath="";
    private ArrayList<CustomEmail> emailList;
    private  String senderName;

    public EmailRequest(String to, String subject, String body, String attachmentPath, String senderName) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.attachmentPath = attachmentPath;
        this.senderName=senderName;
    }
    // Getters and Setters
    public String getTo() { return to; }

    public ArrayList<CustomEmail> getEmailList() {
        return emailList;
    }

    public void setEmailList(ArrayList<CustomEmail> emailList) {
        this.emailList = emailList;
    }

    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}

