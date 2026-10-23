package com.example.EasyApply.Services;

import com.example.EasyApply.Data.CompanyDetailsRequest;
import com.example.EasyApply.Data.CustomEmail;
import com.example.EasyApply.Data.EmailAttachment;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * In-process replacement for the former RabbitMQ consumer that auto-emailed scrape results.
 */
@Service
public class AutoEmailService {

    private final GmailService gmailService;

    @Value("${easyapply.auto-email.oauth-token:}")
    private String oAuthToken;

    @Value("${easyapply.auto-email.resume-path:}")
    private String resumePath;

    @Value("${easyapply.auto-email.sender-name:}")
    private String senderName;

    @Value("${easyapply.auto-email.enabled:false}")
    private boolean enabled;

    public AutoEmailService(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    public void sendForScrapedCompanies(HashMap<String, CompanyDetailsRequest> emailsMap) {
        if (!enabled) {
            return;
        }
        if (emailsMap == null || emailsMap.isEmpty()) {
            return;
        }
        if (oAuthToken == null || oAuthToken.isBlank()) {
            System.err.println("Auto-email skipped: easyapply.auto-email.oauth-token is not set");
            return;
        }

        String body = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                <p>Dear Sir or Madam,</p>
                <p>As a fifth-year software engineering student, I have acquired solid experience in backend, mobile development, and DevOps, mastering technologies such as Java, Spring Boot, and Kotlin. Passionate about innovation, I am eager to join <strong>companyName</strong> to actively contribute to your projects and further develop my skills.</p>
                <p><strong>Practical Experience:</strong> Developing complex applications and collaborating in agile teams.<br>
                <strong>Technologies:</strong> Expertise in backend development (Java, Spring Boot), mobile development (Kotlin), and DevOps.<br>
                <strong>Motivation:</strong> Dedicated and ready to bring my creativity and expertise.</p>
                <p>I would be delighted to discuss in more detail how my skills and experience can contribute to your organization.</p>
                <p>Sincerely,<br>[Your Name]</p>
                </body>
                </html>
                """;

        ArrayList<CustomEmail> list = new ArrayList<>();
        for (Map.Entry<String, CompanyDetailsRequest> entry : emailsMap.entrySet()) {
            list.add(new CustomEmail(entry.getKey(), entry.getValue().getName()));
        }

        String accessToken = oAuthToken.replace("Bearer ", "");
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

        gmailService.sendAutomatedEmails(
                "Seeking and end of study internship",
                body,
                loadAttachment(resumePath),
                credential,
                list,
                senderName
        );
    }

    private EmailAttachment loadAttachment(String pathValue) {
        if (pathValue == null || pathValue.isBlank()) {
            return EmailAttachment.empty();
        }
        try {
            Path path = Paths.get(pathValue);
            if (!Files.isRegularFile(path)) {
                return EmailAttachment.empty();
            }
            String name = path.getFileName() != null ? path.getFileName().toString() : "resume.pdf";
            return new EmailAttachment(Files.readAllBytes(path), name);
        } catch (IOException e) {
            System.err.println("Auto-email attachment skipped: " + e.getMessage());
            return EmailAttachment.empty();
        }
    }
}
