package org.example.location_voiture.config;

import org.example.location_voiture.model.User;
import org.example.location_voiture.model.enums.Role;
import org.example.location_voiture.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@locationvoiture.com";
        String adminPassword = "AdminPassword123!"; // Vous pouvez changer ceci ou utiliser une variable d'environnement

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .nomComplet("Administrateur Serveur")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .actif(true)
                    .build();

            userRepository.save(admin);
            log.info("L'utilisateur administrateur par défaut a été créé avec succès.");
            log.info("Email : {}", adminEmail);
            log.info("Mot de passe : {}", adminPassword);
        } else {
            log.info("L'utilisateur administrateur existe déjà.");
        }
    }
}
