package org.example.location_voiture.controller;

import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.service.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/voitures")
public class VoitureController {

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private org.example.location_voiture.service.FileStorageService fileStorageService;

    @GetMapping
    public String listVoitures(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String carburant,
            @RequestParam(required = false) Double prixMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Voiture> voiturePage = voitureService.searchVoitures(search, categorie, transmission, carburant, prixMax, pageable);
        
        model.addAttribute("voitures", voiturePage.getContent());
        model.addAttribute("voiturePage", voiturePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", voiturePage.getTotalPages());
        
        // Pass filter values back to view
        model.addAttribute("search", search);
        model.addAttribute("categorie", categorie);
        model.addAttribute("transmission", transmission);
        model.addAttribute("carburant", carburant);
        model.addAttribute("prixMax", prixMax);
        
        return "voitures/liste";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("voiture", new Voiture());
        return "voitures/ajouter";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ajouter")
    public String addVoiture(@Valid @ModelAttribute("voiture") Voiture voiture, 
                           BindingResult result,
                           @RequestParam(value = "imageFiles", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> imageFiles,
                           Model model) {
        
        if (result.hasErrors()) {
            return "voitures/ajouter";
        }

        java.util.List<String> finalImages = new java.util.ArrayList<>();
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String fileUrl = fileStorageService.storeFile(file);
                    finalImages.add(fileUrl);
                }
            }
        }
        
        if (!finalImages.isEmpty()) {
            voiture.setImages(finalImages);
        }
        
        voitureService.saveVoiture(voiture);
        return "redirect:/voitures";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("voiture", voitureService.getVoitureById(id));
        return "voitures/modifier";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/modifier/{id}")
    public String updateVoiture(@PathVariable Long id, 
                              @Valid @ModelAttribute("voiture") Voiture voiture,
                              BindingResult result,
                              @RequestParam(value = "existingImages", required = false) java.util.List<String> existingImages,
                              @RequestParam(value = "imageFiles", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> imageFiles,
                              Model model) {

        if (result.hasErrors()) {
            return "voitures/modifier";
        }

        java.util.List<String> finalImages = new java.util.ArrayList<>();
        
        // Preserve existing images that were not deleted
        if (existingImages != null) {
            finalImages.addAll(existingImages);
        }

        // Add newly uploaded images
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String fileUrl = fileStorageService.storeFile(file);
                    finalImages.add(fileUrl);
                }
            }
        }
        
        // Update the voiture object's images list
        voiture.setImages(finalImages);

        voitureService.updateVoiture(id, voiture);
        return "redirect:/voitures";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/supprimer/{id}")
    public String deleteVoiture(@PathVariable Long id) {
        voitureService.deleteVoiture(id);
        return "redirect:/voitures";
    }

    @GetMapping("/{id}")
    public String detailsVoiture(@PathVariable Long id, Model model) {
        model.addAttribute("voiture", voitureService.getVoitureById(id));
        return "voitures/details";
    }
}