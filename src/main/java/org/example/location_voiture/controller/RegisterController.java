package org.example.location_voiture.controller;

import org.example.location_voiture.model.Client;
import org.example.location_voiture.model.User;
import org.example.location_voiture.model.enums.Role;
import org.example.location_voiture.repository.ClientRepository;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private org.example.location_voiture.service.EmailService emailService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @Transactional
    @PostMapping("/register")
    public String processRegistration(@RequestParam("nom") String nom,
                                      @RequestParam("prenom") String prenom,
                                      @RequestParam("email") String email,
                                      @RequestParam("telephone") String telephone,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
            return "redirect:/register";
        }

        if (!nom.matches("^[a-zA-ZÀ-ÿ\\s\\'-]+$") || !prenom.matches("^[a-zA-ZÀ-ÿ\\s\\'-]+$")) {
            redirectAttributes.addFlashAttribute("error", "Le nom ou le prénom contient des caractères non autorisés.");
            return "redirect:/register";
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères.");
            return "redirect:/register";
        }

        java.util.Optional<User> existingUser = userService.findByEmail(email);
        Client existingClient = clientRepository.findByEmail(email);

        if (existingUser.isPresent() || existingClient != null) {
            User user = existingUser.orElse(existingClient != null ? existingClient.getUtilisateur() : null);
            
            if (user != null && !user.isActif()) {
                // Si le compte existe mais n'est pas encore activé, on renvoie un nouveau token
                String newToken = java.util.UUID.randomUUID().toString();
                user.setVerificationToken(newToken);
                userService.saveUser(user);
                
                String verificationUrl = "http://localhost:8080/verify-account?token=" + newToken;
                String subject = "Activation de votre compte - Location Voiture";
                String htmlContent = emailService.buildHtmlMessage("Activation de Compte", 
                              "Bonjour " + prenom + ",\n\n" +
                              "Un compte non-actif existe déjà avec cet email. Veuillez cliquer sur le lien ci-dessous pour activer votre compte :\n\n" +
                              "<a href=\"" + verificationUrl + "\" style=\"display: inline-block; padding: 12px 25px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 8px; font-weight: bold;\">Activer mon compte</a>\n\n" +
                              "Si le bouton ne fonctionne pas, copiez ce lien : " + verificationUrl);
                
                emailService.sendHtmlEmail(email, subject, htmlContent);
                
                redirectAttributes.addFlashAttribute("message", "Un compte existait déjà mais n'était pas activé. Un nouveau lien de vérification vous a été envoyé.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé par un compte actif ou les données sont inconsistantes.");
                return "redirect:/register";
            }
        }

        try {
            User newUser = new User();
            newUser.setNomComplet(prenom + " " + nom);
            newUser.setEmail(email);
            newUser.setPassword(password); // UserService s'occupera du hachage
            newUser.setRole(Role.CLIENT);
            newUser.setActif(false);
            
            String token = java.util.UUID.randomUUID().toString();
            newUser.setVerificationToken(token);

            User savedUser = userService.saveUser(newUser);

            Client newClient = new Client();
            newClient.setNom(nom);
            newClient.setPrenom(prenom);
            newClient.setEmail(email);
            newClient.setTelephone(telephone);
            newClient.setUtilisateur(savedUser);

            clientRepository.save(newClient);

            // Envoyer l'email de vérification
            String verificationUrl = "http://localhost:8080/verify-account?token=" + token;
            String subject = "Activez votre compte - Location Voiture";
            String htmlContent = emailService.buildHtmlMessage("Bienvenue !", 
                          "Merci de vous être inscrit. Veuillez cliquer sur le bouton ci-dessous para activer votre compte :\n\n" +
                          "<a href=\"" + verificationUrl + "\" style=\"display: inline-block; padding: 12px 25px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 8px; font-weight: bold;\">Activer mon compte</a>\n\n" +
                          "Si le bouton ne fonctionne pas, copiez ce lien : " + verificationUrl);
            
            emailService.sendHtmlEmail(email, subject, htmlContent);

            redirectAttributes.addFlashAttribute("message", "Compte créé avec succès ! Veuillez vérifier votre email pour activer votre compte.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du compte: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-account")
    public String verifyAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyAccount(token);
        if (verified) {
            redirectAttributes.addFlashAttribute("message", "Votre compte a été activé avec succès ! Vous pouvez maintenant vous connecter.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Lien de vérification invalide ou expiré.");
        }
        return "redirect:/login";
    }
}
