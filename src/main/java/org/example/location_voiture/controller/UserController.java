package org.example.location_voiture.controller;

import org.example.location_voiture.model.User;
import org.example.location_voiture.model.enums.Role;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("page", "users");
        return "users/liste";
    }

    @GetMapping("/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "users/ajouter";
    }

    @PostMapping("/ajouter")
    public String addUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/ajouter";
        }
        userService.saveUser(user);
        return "redirect:/users";
    }

    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", Role.values());
        return "users/modifier";
    }

    @PostMapping("/modifier/{id}")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute("user") User userDetails, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/modifier";
        }
        User user = userService.getUserById(id);
        user.setNomComplet(userDetails.getNomComplet());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setActif(userDetails.isActif());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }
        userService.saveUser(user);
        return "redirect:/users";
    }

    @GetMapping("/supprimer/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
}
