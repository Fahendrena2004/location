package org.example.location_voiture.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String attachmentName) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            if (attachment != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment));
            }

            mailSender.send(message);
            System.out.println("Email envoyé avec succès à " + to);
        } catch (Exception e) {
            System.err.println("Erreur ignorée lors de l'envoi de l'email à " + to + " : " + e.getMessage());
            // On ne throw pas de RuntimeException ici pour ne pas bloquer l'application en cas de mauvais serveur SMTP (smtp.example.com)
        }
    }

    public void sendTextEmail(String to, String subject, String body) {
        sendEmailWithAttachment(to, subject, body, null, null);
    }
}
