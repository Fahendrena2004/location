package org.example.location_voiture.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    @org.springframework.scheduling.annotation.Async
    public void sendTextEmail(String to, String subject, String body) {
        sendEmailWithAttachment(to, subject, body, false, null, null);
    }

    @org.springframework.scheduling.annotation.Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        sendEmailWithAttachment(to, subject, htmlBody, true, null, null);
    }

    @org.springframework.scheduling.annotation.Async
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName) {
        sendEmailWithAttachment(to, subject, body, false, attachment, attachmentName);
    }

    public void sendEmailWithAttachment(String to, String subject, String content, boolean isHtml, byte[] attachment, String attachmentName) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(Objects.requireNonNull(to));
            helper.setSubject(Objects.requireNonNull(subject));
            helper.setText(Objects.requireNonNull(content), isHtml);

            if (attachment != null) {
                helper.addAttachment(Objects.requireNonNull(attachmentName), new ByteArrayResource(attachment));
            }

            mailSender.send(message);
            System.out.println("[ASYNC] Email envoyé avec succès à " + to);
        } catch (Exception e) {
            System.err.println("[ASYNC] Erreur ignorée lors de l'envoi de l'email à " + to + " : " + e.getMessage());
        }
    }

    public String buildHtmlMessage(String title, String message) {
        return "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">" +
                "  <div style=\"background-color: #2563eb; color: white; padding: 30px; text-align: center;\">" +
                "    <h1 style=\"margin: 0; font-size: 24px;\">Location Voiture Premium</h1>" +
                "  </div>" +
                "  <div style=\"padding: 40px; color: #1e293b; line-height: 1.6;\">" +
                "    <h2 style=\"color: #2563eb; margin-top: 0;\">" + title + "</h2>" +
                "    <p style=\"font-size: 16px;\">" + message.replace("\n", "<br>") + "</p>" +
                "    <div style=\"margin-top: 40px; padding-top: 20px; border-top: 1px solid #f1f5f9; font-size: 12px; color: #64748b; text-align: center;\">" +
                "      Ceci est un message automatique, merci de ne pas y répondre.<br>" +
                "      &copy; 2026 Location Voiture Premium. Tous droits réservés." +
                "    </div>" +
                "  </div>" +
                "</div>";
    }
}
