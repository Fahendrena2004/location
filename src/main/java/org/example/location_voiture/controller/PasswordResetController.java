package org.example.location_voiture.controller;

import org.example.location_voiture.model.User;
import org.example.location_voiture.service.EmailService;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        if (!userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Désolé, aucune adresse correspondante n'a été trouvée.");
            return "redirect:/forgot-password";
        }

        try {
            User user = userService.getUserByEmail(email);
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            String subject = "Réinitialisation de votre mot de passe - Location Voiture";
            String htmlContent = emailService.buildHtmlMessage("Réinitialisation de mot de passe", 
                          "Vous avez demandé la réinitialisation de votre mot de passe. Veuillez cliquer sur le bouton ci-dessous pour choisir un nouveau mot de passe :\n\n" +
                          "<a href=\"" + resetUrl + "\" style=\"display: inline-block; padding: 12px 25px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 8px; font-weight: bold;\">Réinitialiser mon mot de passe</a>\n\n" +
                          "Ce lien est valable pendant 30 minutes.\n\n" +
                          "Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet e-mail.");
            
            emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent);
            
            redirectAttributes.addFlashAttribute("message", "Un e-mail avec un lien de réinitialisation vous a été envoyé.");
            return "redirect:/forgot-password";
        } catch (Exception e) {
            e.printStackTrace(); // Mba ho hitantsika ao amin'ny terminal ny antony marina
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de l'envoi de l'e-mail : " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        String result = userService.validatePasswordResetToken(token);
        if (result != null) {
            model.addAttribute("error", "Lien de réinitialisation invalide ou expiré.");
            return "redirect:/login?error=invalidToken";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, 
                                       @RequestParam("password") String password, 
                                       RedirectAttributes redirectAttributes) {
        String result = userService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("error", "Lien de réinitialisation invalide ou expiré.");
            return "redirect:/login";
        }

        User user = userService.getUserByPasswordResetToken(token);
        if (user != null) {
            userService.changeUserPassword(user, password);
            // Supprimer le token après utilisation pour plus de sécurité
            userService.deletePasswordResetToken(token);
            redirectAttributes.addFlashAttribute("message", "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
        }
        
        return "redirect:/login";
    }
}
