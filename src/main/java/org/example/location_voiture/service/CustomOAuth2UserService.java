package org.example.location_voiture.service;

import org.example.location_voiture.model.enums.Role;
import org.example.location_voiture.model.User;
import org.example.location_voiture.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Register new user automatically
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setNomComplet(name);
            newUser.setPassword(""); // No password for OAuth users
            newUser.setRole(Role.CLIENT);
            newUser.setActif(true); // OAuth verified emails are active
            userService.saveUser(newUser);
        }
        
        return oauth2User;
    }
}
