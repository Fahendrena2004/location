package org.example.location_voiture.controller;

import org.example.location_voiture.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/email")
    public String testEmail(@RequestParam String to) {
        try {
            emailService.sendTextEmail(to, "Test SMTP - Location Voiture", 
                "Ceci est un email de test pour vérifier la configuration SMTP de votre application.\n\n" +
                "Si vous recevez cet email, cela signifie que la configuration est correcte !");
            return "Email de test envoyé à " + to + ". Vérifiez votre boîte de réception.";
        } catch (Exception e) {
            return "Erreur lors de l'envoi de l'email : " + e.getMessage();
        }
    }
}
