package org.example.location_voiture.controller;

import org.example.location_voiture.model.Entretien;
import org.example.location_voiture.service.EntretienService;
import org.example.location_voiture.service.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/entretien")
public class EntretienController {

    @Autowired
    private EntretienService entretienService;

    @Autowired
    private VoitureService voitureService;

    @GetMapping
    public String listEntretiens(@RequestParam(required = false) Long voitureId, Model model) {
        if (voitureId != null) {
            model.addAttribute("entretiens", entretienService.getEntretiensByVoiture(voitureId));
            model.addAttribute("selectedVoitureId", voitureId);
        } else {
            model.addAttribute("entretiens", entretienService.getAllEntretiens());
        }
        model.addAttribute("voitures", voitureService.getAllVoitures());
        return "entretien/liste";
    }

    @GetMapping("/planifier")
    public String showAddForm(Model model) {
        model.addAttribute("entretien", new Entretien());
        model.addAttribute("voitures", voitureService.getAllVoitures());
        return "entretien/planifier";
    }

    @PostMapping("/planifier")
    public String addEntretien(@ModelAttribute Entretien entretien) {
        entretienService.saveEntretien(entretien);
        return "redirect:/entretien";
    }

    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("entretien", entretienService.getEntretienById(id));
        model.addAttribute("voitures", voitureService.getAllVoitures());
        return "entretien/modifier";
    }

    @PostMapping("/modifier/{id}")
    public String updateEntretien(@PathVariable Long id, @ModelAttribute Entretien entretien) {
        entretienService.updateEntretien(id, entretien);
        return "redirect:/entretien";
    }

    @GetMapping("/supprimer/{id}")
    public String deleteEntretien(@PathVariable Long id) {
        entretienService.deleteEntretien(id);
        return "redirect:/entretien";
    }

    @GetMapping("/{id}")
    public String detailsEntretien(@PathVariable Long id, Model model) {
        model.addAttribute("entretien", entretienService.getEntretienById(id));
        return "entretien/details";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id) {
        entretienService.toggleStatus(id);
        return "redirect:/entretien";
    }
}