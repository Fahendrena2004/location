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
        try {
            User user = userService.getUserByEmail(email);
            if (user != null) {
                String token = UUID.randomUUID().toString();
                userService.createPasswordResetTokenForUser(user, token);
                
                String resetUrl = "http://localhost:8080/reset-password?token=" + token;
                emailService.sendTextEmail(user.getEmail(), "Réinitialisation de mot de passe", 
                        "Pour réinitialiser votre mot de passe, cliquez sur ce lien : \n" + resetUrl);
            }
        } catch (Exception e) {
            // Ignorer si l'utilisateur n'est pas trouvé pour des raisons de sécurité
        }
        
        redirectAttributes.addFlashAttribute("message", "Si un compte est associé à cette adresse, un e-mail avec un lien de réinitialisation vous a été envoyé.");
        return "redirect:/login";
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
            redirectAttributes.addFlashAttribute("message", "Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.");
        }
        
        return "redirect:/login";
    }
}
