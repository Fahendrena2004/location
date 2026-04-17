package org.example.location_voiture.controller;

import org.example.location_voiture.model.Client;
import org.example.location_voiture.model.User;
import org.example.location_voiture.service.ClientService;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listClients(Model model) {
        model.addAttribute("clients", clientService.getAllClients());
        return "clients/liste";
    }

    @GetMapping("/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("users", userService.getAllUsers());
        return "clients/ajouter";
    }

    @PostMapping("/ajouter")
    public String addClient(@Valid @ModelAttribute("client") Client client,
                            BindingResult result,
                            @RequestParam(value = "userId", required = false) Long userId,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "clients/ajouter";
        }
        if (userId != null) {
            User user = userService.getUserById(userId);
            client.setUtilisateur(user);
        }
        clientService.saveClient(client);
        return "redirect:/clients";
    }

    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClientById(id));
        return "clients/modifier";
    }

    @PostMapping("/modifier/{id}")
    public String updateClient(@PathVariable Long id, @Valid @ModelAttribute("client") Client client, BindingResult result) {
        if (result.hasErrors()) {
            return "clients/modifier";
        }
        clientService.updateClient(id, client);
        return "redirect:/clients";
    }

    @GetMapping("/supprimer/{id}")
    public String deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return "redirect:/clients";
    }

    @GetMapping("/{id}")
    public String detailsClient(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClientById(id));
        return "clients/details";
    }
}