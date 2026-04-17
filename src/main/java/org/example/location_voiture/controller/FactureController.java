package org.example.location_voiture.controller;

import org.example.location_voiture.model.Facture;
import org.example.location_voiture.model.enums.StatutFacture;
import org.example.location_voiture.service.FactureService;
import org.example.location_voiture.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.location_voiture.service.EmailService;
import org.example.location_voiture.service.PdfService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import org.example.location_voiture.service.UserService;
import org.example.location_voiture.model.User;

@Controller
@RequestMapping("/factures")
public class FactureController {

    @Autowired
    private UserService userService;

    @Autowired
    private FactureService factureService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private org.example.location_voiture.service.SettingService settingService;

    @Autowired
    private org.example.location_voiture.service.NotificationService notificationService;

    @GetMapping
    public String listFactures(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        if (user.getRole() == org.example.location_voiture.model.enums.Role.ADMIN) {
            java.util.List<Facture> factures = factureService.getAllFactures();
            model.addAttribute("factures", factures);
            
            double totalPaid = factures.stream()
                .filter(f -> f.getStatut() == StatutFacture.PAYEE)
                .mapToDouble(f -> f.getMontantTTC() != null ? f.getMontantTTC() : 0.0)
                .sum();
            double totalPending = factures.stream()
                .filter(f -> f.getStatut() == StatutFacture.EN_ATTENTE)
                .mapToDouble(f -> f.getMontantTTC() != null ? f.getMontantTTC() : 0.0)
                .sum();
            
            model.addAttribute("totalPaid", totalPaid);
            model.addAttribute("totalPending", totalPending);
        } else if (user.getClient() != null) {
            model.addAttribute("factures", factureService.getFacturesByClient(user.getClient()));
        } else {
            model.addAttribute("factures", java.util.Collections.emptyList());
        }
        return "factures/liste";
    }




    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/generer")
    public String showGeneralGenerationForm(Model model) {
        model.addAttribute("locations", locationService.getLocationsPayantes());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("statuts", StatutFacture.values());
        return "factures/generer";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/generer/{locationId}")
    public String showGenerationForm(@PathVariable Long locationId, Model model) {
        model.addAttribute("selectedLocationId", locationId);
        model.addAttribute("locations", locationService.getLocationsPayantes());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("statuts", StatutFacture.values());
        return "factures/generer";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generer")
    public String generateFacture(@RequestParam Long locationId,
                                @RequestParam LocalDate dateEmission,
                                @RequestParam StatutFacture statut) {
        // Paramètres par défaut pour devise et compte si non spécifiés dans la nouvelle UI
        org.example.location_voiture.model.enums.Currency devise = org.example.location_voiture.model.enums.Currency.MGA;
        Facture f = factureService.generateFactureForLocation(locationId, devise, null);
        f.setDateEmission(dateEmission);
        f.setStatut(statut);
        factureService.saveFacture(f);
        return "redirect:/factures";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("facture", factureService.getFactureById(id));
        return "factures/modifier";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/modifier/{id}")
    public String updateFacture(@PathVariable Long id, @ModelAttribute Facture facture) {
        factureService.updateFacture(id, facture);
        return "redirect:/factures";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/supprimer/{id}")
    public String deleteFacture(@PathVariable Long id) {
        factureService.deleteFacture(id);
        return "redirect:/factures";
    }

    @GetMapping("/{id}")
    public String detailsFacture(@PathVariable Long id, Model model, java.security.Principal principal) {
        Facture facture = factureService.getFactureById(id);
        User user = userService.getUserByEmail(principal.getName());
        
        // Security check
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN) {
            if (user.getClient() == null || !facture.getLocation().getClient().getId().equals(user.getClient().getId())) {
                return "redirect:/access-denied";
            }
        }
        
        model.addAttribute("facture", facture);
        return "factures/details";
    }

    @GetMapping("/imprimer/{id}")
    public String imprimerFacture(@PathVariable Long id, Model model, java.security.Principal principal) {
        Facture facture = factureService.getFactureById(id);
        User user = userService.getUserByEmail(principal.getName());
        
        // Security check
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN) {
            if (user.getClient() == null || !facture.getLocation().getClient().getId().equals(user.getClient().getId())) {
                return "redirect:/access-denied";
            }
        }
        
        model.addAttribute("facture", facture);
        return "factures/imprimer";
    }

    @GetMapping("/telecharger/{id}")
    public ResponseEntity<InputStreamResource> downloadFacture(@PathVariable Long id, 
                                                              @RequestParam(required = false, defaultValue = "MGA") String devise, 
                                                              Principal principal) {
        Facture facture = factureService.getFactureById(id);
        User user = userService.getUserByEmail(principal.getName());
        
        // Security Check: If client, must be their own invoice
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN) {
            if (user.getClient() == null || !facture.getLocation().getClient().getId().equals(user.getClient().getId())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
        }

        Facture pdfFacture = facture;
        if ("EUR".equalsIgnoreCase(devise)) {
            pdfFacture = new Facture();
            org.springframework.beans.BeanUtils.copyProperties(facture, pdfFacture);
            double rate = settingService.getExchangeRate();
            if (rate > 0) {
                pdfFacture.setDevise(org.example.location_voiture.model.enums.Currency.EUR);
                pdfFacture.setMontantHT(facture.getMontantHT() / rate);
                pdfFacture.setMontantTTC(facture.getMontantTTC() / rate);
                if (facture.getPenalite() != null) {
                    pdfFacture.setPenalite(facture.getPenalite() / rate);
                }
            }
        } else {
            // Re-assign explicitly in case base record is null for some reason
            pdfFacture.setDevise(org.example.location_voiture.model.enums.Currency.MGA);
        }

        ByteArrayInputStream bis = pdfService.generateInvoicePdf(pdfFacture);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=facture_" + facture.getNumeroFacture() + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/envoyer/{id}")
    public String sendFactureByEmail(@PathVariable Long id, 
                                     @RequestParam(required = false, defaultValue = "MGA") String devise, 
                                     RedirectAttributes redirectAttributes) {
        Facture facture = factureService.getFactureById(id);
        String to = facture.getLocation().getClient().getEmail();
        
        if (to == null || to.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Le client n'a pas d'adresse e-mail renseignée.");
            return "redirect:/factures";
        }

        Facture pdfFacture = facture;
        if ("EUR".equalsIgnoreCase(devise)) {
            pdfFacture = new Facture();
            org.springframework.beans.BeanUtils.copyProperties(facture, pdfFacture);
            double rate = settingService.getExchangeRate();
            if (rate > 0) {
                pdfFacture.setDevise(org.example.location_voiture.model.enums.Currency.EUR);
                pdfFacture.setMontantHT(facture.getMontantHT() / rate);
                pdfFacture.setMontantTTC(facture.getMontantTTC() / rate);
                if (facture.getPenalite() != null) {
                    pdfFacture.setPenalite(facture.getPenalite() / rate);
                }
            }
        } else {
            pdfFacture.setDevise(org.example.location_voiture.model.enums.Currency.MGA);
        }

        byte[] pdfBytes = pdfService.generateInvoicePdfBytes(pdfFacture);
        
        String subject = "Votre facture " + facture.getNumeroFacture();
        String body = "Bonjour " + facture.getLocation().getClient().getNomComplet() + ",\n\n" +
                      "Veuillez trouver ci-joint votre facture concernant votre location de voiture.\n\n" +
                      "Cordialement,\nL'équipe Location Voiture";
        
        emailService.sendEmailWithAttachment(to, subject, body, pdfBytes, "facture_" + facture.getNumeroFacture() + ".pdf");
        
        // Notification In-App pour le client
        if (facture.getLocation().getClient() != null && facture.getLocation().getClient().getUtilisateur() != null) {
            notificationService.createNotification(
                "Facture Envoyée",
                "Votre facture #" + facture.getNumeroFacture() + " a été envoyée à votre adresse e-mail (" + to + ").",
                "/factures",
                facture.getLocation().getClient().getUtilisateur()
            );
        }

        redirectAttributes.addFlashAttribute("success", "La facture a été envoyée avec succès à " + to);
        return "redirect:/factures";
    }
}
