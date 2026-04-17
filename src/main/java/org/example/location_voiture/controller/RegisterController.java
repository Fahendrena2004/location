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

        if (userService.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé.");
            return "redirect:/register";
        }

        try {
            User newUser = new User();
            newUser.setNomComplet(prenom + " " + nom);
            newUser.setEmail(email);
            newUser.setPassword(password); // UserService s'occupera du hachage
            newUser.setRole(Role.CLIENT);
            newUser.setActif(true);

            User savedUser = userService.saveUser(newUser);

            Client newClient = new Client();
            newClient.setNom(nom);
            newClient.setPrenom(prenom);
            newClient.setEmail(email);
            newClient.setTelephone(telephone);
            newClient.setUtilisateur(savedUser);

            clientRepository.save(newClient);

            redirectAttributes.addFlashAttribute("message", "Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création du compte: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
