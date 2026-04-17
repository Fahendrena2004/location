package org.example.location_voiture.controller;

import org.example.location_voiture.model.Chauffeur;
import org.example.location_voiture.service.ChauffeurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/chauffeurs")
public class ChauffeurController {

    @Autowired
    private ChauffeurService chauffeurService;

    @GetMapping
    public String listChauffeurs(Model model) {
        model.addAttribute("chauffeurs", chauffeurService.getAllChauffeurs());
        model.addAttribute("page", "chauffeurs");
        return "chauffeurs/liste";
    }

    @GetMapping("/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("chauffeur", new Chauffeur());
        return "chauffeurs/ajouter";
    }

    @PostMapping("/ajouter")
    public String addChauffeur(@Valid @ModelAttribute("chauffeur") Chauffeur chauffeur, BindingResult result) {
        if (result.hasErrors()) {
            return "chauffeurs/ajouter";
        }
        chauffeurService.saveChauffeur(chauffeur);
        return "redirect:/chauffeurs";
    }

    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("chauffeur", chauffeurService.getChauffeurById(id));
        return "chauffeurs/modifier";
    }

    @PostMapping("/modifier/{id}")
    public String updateChauffeur(@PathVariable Long id, @Valid @ModelAttribute("chauffeur") Chauffeur chauffeurDetails, BindingResult result) {
        if (result.hasErrors()) {
            return "chauffeurs/modifier";
        }
        Chauffeur chauffeur = chauffeurService.getChauffeurById(id);
        chauffeur.setNom(chauffeurDetails.getNom());
        chauffeur.setPrenom(chauffeurDetails.getPrenom());
        chauffeur.setTelephone(chauffeurDetails.getTelephone());
        chauffeur.setNumeroPermis(chauffeurDetails.getNumeroPermis());
        chauffeur.setDisponible(chauffeurDetails.isDisponible());
        chauffeurService.saveChauffeur(chauffeur);
        return "redirect:/chauffeurs";
    }

    @GetMapping("/supprimer/{id}")
    public String deleteChauffeur(@PathVariable Long id) {
        chauffeurService.deleteChauffeur(id);
        return "redirect:/chauffeurs";
    }
}
