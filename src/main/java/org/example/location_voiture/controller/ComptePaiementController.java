package org.example.location_voiture.controller;

import org.example.location_voiture.model.ComptePaiement;
import org.example.location_voiture.service.ComptePaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/comptes")
@PreAuthorize("hasRole('ADMIN')")
public class ComptePaiementController {

    @Autowired
    private ComptePaiementService compteService;

    @GetMapping
    public String listComptes(Model model) {
        model.addAttribute("comptes", compteService.getAllComptes());
        model.addAttribute("page", "comptes");
        return "admin/comptes/liste";
    }

    @GetMapping("/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("compte", new ComptePaiement());
        return "admin/comptes/ajouter";
    }

    @PostMapping("/ajouter")
    public String addCompte(@ModelAttribute ComptePaiement compte) {
        compteService.saveCompte(compte);
        return "redirect:/admin/comptes";
    }

    @GetMapping("/supprimer/{id}")
    public String deleteCompte(@PathVariable Long id) {
        compteService.deleteCompte(id);
        return "redirect:/admin/comptes";
    }

    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("compte", compteService.getCompteById(id));
        return "admin/comptes/modifier";
    }

    @PostMapping("/modifier/{id}")
    public String updateCompte(@PathVariable Long id, @ModelAttribute ComptePaiement compte) {
        compteService.saveCompte(compte);
        return "redirect:/admin/comptes";
    }
}
