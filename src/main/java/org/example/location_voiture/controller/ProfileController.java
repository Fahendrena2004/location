package org.example.location_voiture.controller;

import org.example.location_voiture.model.User;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("page", "profile");
        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String nomComplet, 
                               @RequestParam String email, 
                               Principal principal, 
                               RedirectAttributes ra) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            
            // Check if email is being changed and if new email already exists
            if (!user.getEmail().equals(email) && userService.existsByEmail(email)) {
                ra.addFlashAttribute("error", "Cet email est déjà utilisé par un autre compte.");
                return "redirect:/profile";
            }
            
            user.setNomComplet(nomComplet);
            user.setEmail(email);
            userService.saveUser(user);
            
            ra.addFlashAttribute("success", "Profil mis à jour avec succès.");
            // If email changed, the user might need to re-login depending on security config
            // but usually Principal will just have the old email until session refresh.
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes ra) {
        User user = userService.getUserByEmail(principal.getName());
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "Le mot de passe actuel est incorrect.");
            return "redirect:/profile";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas.");
            return "redirect:/profile";
        }
        
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Le nouveau mot de passe doit faire au moins 6 caractères.");
            return "redirect:/profile";
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);
        
        ra.addFlashAttribute("success", "Mot de passe modifié avec succès.");
        return "redirect:/profile";
    }
}
