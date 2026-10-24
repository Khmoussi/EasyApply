package com.example.EasyApply.Controllers;

import com.example.EasyApply.Data.CustomEmail;
import com.example.EasyApply.Data.CustomEmailResponse;
import com.example.EasyApply.Data.EmailAttachment;
import com.example.EasyApply.Data.EmailRequest;
import com.example.EasyApply.Services.BillingService;
import com.example.EasyApply.Services.ContactLibraryService;
import com.example.EasyApply.Services.GmailService;
import com.example.EasyApply.Services.UserAccountService;
import com.example.EasyApply.domain.AppUser;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/easyapply")
public class EmailController {

    @Autowired
    private GmailService gmailService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private ContactLibraryService contactLibraryService;

    @Autowired
    private BillingService billingService;

    @GetMapping("/ss")
    public String a() {
        return "ss";
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(
            @RequestBody EmailRequest emailRequest,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient googleClient
    ) {
        try {
            Credential credential = credentialFrom(googleClient);
            if (credential == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
            }
            gmailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody(), credential);
            return ResponseEntity.ok("Email sent successfully!");
        } catch (MessagingException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping(value = "/sendEmailWithAttachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendEmailWithAttachment(
            @RequestPart("payload") EmailRequest emailRequest,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient googleClient
    ) {
        try {
            Credential credential = credentialFrom(googleClient);
            if (credential == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
            }
            EmailAttachment emailAttachment = toEmailAttachment(attachment);
            if (attachment != null && !attachment.isEmpty() && !emailAttachment.isPresent()) {
                return ResponseEntity.badRequest().body("Attachment must be a PDF file.");
            }
            gmailService.sendEmailWithAttachment(
                    emailRequest.getTo(),
                    emailRequest.getSubject(),
                    emailRequest.getBody(),
                    emailAttachment,
                    credential
            );
            return ResponseEntity.ok("Email with attachment sent successfully!");
        } catch (MessagingException | IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to send email with attachment: " + e.getMessage());
        }
    }

    @PostMapping(value = "/sendAutomatedEmailsWithAttachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendAutomatedEmailsWithAttachment(
            @RequestPart("payload") EmailRequest emailRequest,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient googleClient,
            @AuthenticationPrincipal OAuth2User oauthUser
    ) throws IOException {
        Credential credential = credentialFrom(googleClient);
        if (credential == null || oauthUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in with Google first.");
        }
        AppUser user = userAccountService.upsertFromOAuth(oauthUser);
        int recipientCount = emailRequest.getEmailList() != null ? emailRequest.getEmailList().size() : 0;
        billingService.assertCanSend(user, recipientCount);

        EmailAttachment emailAttachment = toEmailAttachment(attachment);
        if (attachment != null && !attachment.isEmpty() && !emailAttachment.isPresent()) {
            return ResponseEntity.badRequest().body("Attachment must be a PDF file.");
        }
        CustomEmailResponse result = gmailService.sendAutomatedEmails(
                emailRequest.getSubject(),
                emailRequest.getBody(),
                emailAttachment,
                credential,
                emailRequest.getEmailList(),
                emailRequest.getSenderName()
        );
        if (result.getSent() != null && !result.getSent().isEmpty()) {
            List<String> sentEmails = result.getSent().stream()
                    .map(CustomEmail::getEmail)
                    .collect(Collectors.toList());
            contactLibraryService.markSent(user, sentEmails);
            billingService.recordSends(user, sentEmails.size());
        }
        if (!result.isStatus()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    private EmailAttachment toEmailAttachment(MultipartFile attachment) throws IOException {
        if (attachment == null || attachment.isEmpty()) {
            return EmailAttachment.empty();
        }
        String originalName = attachment.getOriginalFilename() != null ? attachment.getOriginalFilename() : "resume.pdf";
        String contentType = attachment.getContentType() != null ? attachment.getContentType().toLowerCase(Locale.ROOT) : "";
        boolean pdfByName = originalName.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean pdfByType = contentType.contains("pdf");
        if (!pdfByName && !pdfByType) {
            return EmailAttachment.empty();
        }
        String filename = pdfByName ? originalName : "resume.pdf";
        return new EmailAttachment(attachment.getBytes(), filename);
    }

    private Credential credentialFrom(OAuth2AuthorizedClient googleClient) {
        if (googleClient == null || googleClient.getAccessToken() == null) {
            return null;
        }
        return new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(googleClient.getAccessToken().getTokenValue());
    }
}
