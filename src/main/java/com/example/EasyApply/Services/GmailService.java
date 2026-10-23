package com.example.EasyApply.Services;

import com.example.EasyApply.Data.CustomEmail;
import com.example.EasyApply.Data.CustomEmailResponse;
import com.example.EasyApply.Data.EmailAttachment;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GmailService {

    private static final String APPLICATION_NAME = "EasyApply";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Gmail getGmailService(Credential credential) throws GeneralSecurityException, IOException {
        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public CustomEmailResponse sendAutomatedEmails(
            String subject,
            String bodyText,
            EmailAttachment attachment,
            Credential credential,
            List<CustomEmail> emails,
            String senderName
    ) {
        int poolSize = 10;
        int maxQueueSize = 100;
        ExecutorService executorService = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueueSize)
        );

        List<CustomEmail> sentEmails = new CopyOnWriteArrayList<>();
        List<CustomEmail> failedEmails = new CopyOnWriteArrayList<>();
        AtomicBoolean status = new AtomicBoolean(true);
        EmailAttachment safeAttachment = attachment != null ? attachment : EmailAttachment.empty();

        for (CustomEmail customEmail : emails) {
            executorService.submit(() -> {
                try {
                    // Keep fixed recipient for now (testing).
                    sendEmailWithAttachment(
                            "khmoussiaouina2@gmail.com",
                            subject,
                            personalizeMotivationLetter(bodyText, customEmail.getCompanyName(), senderName),
                            safeAttachment,
                            credential
                    );
                    sentEmails.add(customEmail);
                    System.out.println("Sent email to: " + customEmail.getEmail());
                } catch (GoogleJsonResponseException e) {
                    if (e.getStatusCode() == 401) {
                        e.printStackTrace();
                        status.set(false);
                    }
                    failedEmails.add(customEmail);
                } catch (Exception e) {
                    failedEmails.add(customEmail);
                    e.printStackTrace();
                }
            });
        }

        // Wait until all sends finish — HTTP response includes the final sent/failed lists.
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
                System.err.println("Some tasks were not completed within the timeout.");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }

        return new CustomEmailResponse(status.get(), new ArrayList<>(sentEmails), new ArrayList<>(failedEmails));
    }

    public void sendEmail(String to, String subject, String bodyText, Credential credential)
            throws MessagingException, IOException, GeneralSecurityException {
        Gmail service = getGmailService(credential);
        MimeMessage email = createEmail(to, "me", subject, bodyText);
        Message message = createMessageWithEmail(email);
        service.users().messages().send("me", message).execute();
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String bodyText,
            EmailAttachment attachment,
            Credential credential
    ) throws MessagingException, IOException, GeneralSecurityException {
        if (attachment == null || !attachment.isPresent()) {
            sendEmail(to, subject, bodyText, credential);
            return;
        }

        Gmail service = getGmailService(credential);
        MimeMessage email = createEmailWithAttachment(to, "me", subject, bodyText, attachment);
        Message message = createMessageWithEmail(email);
        service.users().messages().send("me", message).execute();
    }

    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public String personalizeMotivationLetter(String letter, String companyName, String senderName) {
        if (companyName == null || companyName.isEmpty()) {
            companyName = "votre entreprise";
        }
        return letter.replace("companyName", companyName).replace("[Your Name]", senderName);
    }

    private MimeMessage createEmailWithAttachment(
            String to,
            String from,
            String subject,
            String bodyHtml,
            EmailAttachment attachment
    ) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(bodyHtml, "text/html");

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        ByteArrayDataSource dataSource = new ByteArrayDataSource(attachment.content(), "application/pdf");
        attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
        attachmentBodyPart.setFileName(attachment.filename());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlBodyPart);
        multipart.addBodyPart(attachmentBodyPart);

        email.setContent(multipart);
        return email;
    }
}
