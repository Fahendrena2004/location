package org.example.location_voiture.service;

import org.example.location_voiture.model.User;
import org.example.location_voiture.model.PasswordResetToken;
import org.example.location_voiture.repository.UserRepository;
import org.example.location_voiture.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(Objects.requireNonNull(user));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(Objects.requireNonNull(id));
    }

    public User getUserById(Long id) {
        return userRepository.findById(Objects.requireNonNull(id)).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @org.springframework.transaction.annotation.Transactional
    public void createPasswordResetTokenForUser(User user, String token) {
        // Supprimer tous les anciens tokens pour cet utilisateur directement
        tokenRepository.deleteByUser(user);
        tokenRepository.flush();
        
        PasswordResetToken myToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(myToken);
    }

    public String validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passToken = tokenRepository.findByToken(token);
        if (!passToken.isPresent()) {
            return "invalidToken";
        }

        PasswordResetToken resetToken = passToken.get();
        if (resetToken.isExpired()) {
            return "expired";
        }

        return null;
    }

    public User getUserByPasswordResetToken(String token) {
        return tokenRepository.findByToken(token).map(PasswordResetToken::getUser).orElse(null);
    }

    public void changeUserPassword(User user, String password) {
        System.out.println("[INFO] Réinitialisation du mot de passe pour l'utilisateur: " + user.getEmail());
        
        // Hachage direct pour être sûr à 100%
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setActif(true);
        
        userRepository.save(user);
        
        System.out.println("[INFO] Mot de passe changé avec succès !");
        System.out.println("[DEBUG] Hash généré : " + encodedPassword.substring(0, 10) + "...");
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public List<User> getAdmins() {
        return userRepository.findByRole(org.example.location_voiture.model.enums.Role.ADMIN);
    }

    public Optional<User> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    public boolean verifyAccount(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActif(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @org.springframework.transaction.annotation.Transactional
    public void deletePasswordResetToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
}
