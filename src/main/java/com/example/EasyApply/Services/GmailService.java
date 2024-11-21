package com.example.EasyApply.Services;


import com.example.EasyApply.Data.CustomEmail;
import com.example.EasyApply.Data.CustomEmailResponse;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class GmailService {

    private static final String APPLICATION_NAME = "EasyApply";  // Update to your application name
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // Builds and returns a Gmail API client service
    private Gmail getGmailService(Credential credential) throws GeneralSecurityException, IOException {
        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Method to send an email using the Gmail API
    public void sendEmail(String to, String subject, String bodyText, Credential credential)
            throws MessagingException, IOException, GeneralSecurityException {
        Gmail service = getGmailService(credential);

        // Create an email message
        MimeMessage email = createEmail(to, "me", subject, bodyText);

        // Send the email
        Message message = createMessageWithEmail(email);
        service.users().messages().send("me", message).execute();
    }

    // Creates a MimeMessage email
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

    // Encodes a MimeMessage into a Gmail API Message
    private Message createMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }



    // with attachement


    public String personalizeMotivationLetter(String letter, String companyName, String senderName) {
        if(companyName.isEmpty())
            companyName="votre entreprise";
        return letter.replace("companyName", companyName).replace("[Your Name]",senderName);
    }


    public CustomEmailResponse sendAutomatedEmails(String subject, String bodyText, Path attachmentPath,
                                                         Credential credential, List<CustomEmail> emails,String senderName)  {
        int poolSize = 10;
        int maxQueueSize = 100; // Adjust queue size as needed
        ExecutorService executorService = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueueSize)
        );       // equivalent but with limited queue size: ExecutorService executorService = Executors.newFixedThreadPool(10); // Customize pool size as needed



        List<CustomEmail> failedEmails = new CopyOnWriteArrayList<>();
        AtomicBoolean status= new AtomicBoolean(true);
        for (CustomEmail customEmail : emails) {
            executorService.submit(() -> {
                try {
                    sendEmailWithAttachment(customEmail.getEmail(), subject,
                            personalizeMotivationLetter(bodyText, customEmail.getCompanyName(),senderName),
                            attachmentPath, credential);  // Your email sending logic
                    System.out.println("Sent email to: " + customEmail.getEmail());
                } catch (GoogleJsonResponseException e) {
                    if (e.getStatusCode() == 401) {
                        e.printStackTrace();
                        status.set(false);
                    }
                    failedEmails.add(customEmail);

                } catch (Exception e) {

                    failedEmails.add(customEmail); // Catch-all for other exceptions
                    e.printStackTrace();
                }
            });
        }

        // Properly shut down the executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
                System.err.println("Some tasks were not completed within the timeout.");
                executorService.shutdownNow(); // Force shutdown if tasks are still running
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }finally {
            // Properly shut down the executor service
            executorService.shutdown();
        }

        return new CustomEmailResponse(status,failedEmails); // Return the list of emails that failed to send
    }
    public void sendEmailWithAttachment(String to, String subject, String bodyText, Path attachmentPath, Credential credential)
            throws MessagingException, IOException, GeneralSecurityException {
        Gmail service = getGmailService(credential);

        // Create an email message with an attachment
        MimeMessage email = createEmailWithAttachment(to, "me", subject, bodyText, attachmentPath);

        // Send the email
        Message message = createMessageWithEmail(email);
        service.users().messages().send("me", message).execute();
    }

    private MimeMessage createEmailWithAttachment(String to, String from, String subject, String bodyHtml, Path attachmentPath)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);

        // Body part for the main HTML message
        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(bodyHtml, "text/html");

        // Body part for the attachment
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.attachFile(attachmentPath.toFile());

        // Combine HTML and attachment parts into a multipart
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlBodyPart);
        multipart.addBodyPart(attachmentBodyPart);

        // Set the multipart as the email content
        email.setContent(multipart);

        return email;
    }

}
