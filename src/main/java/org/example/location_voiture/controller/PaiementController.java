package org.example.location_voiture.controller;

import org.example.location_voiture.model.Paiement;
import org.example.location_voiture.service.LocationService;
import org.example.location_voiture.service.PaiementService;
import org.example.location_voiture.service.FactureService;
import org.example.location_voiture.service.PdfService;
import org.example.location_voiture.service.EmailService;
import org.example.location_voiture.model.Facture;
import org.example.location_voiture.model.enums.StatutFacture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import org.example.location_voiture.model.Location;
import org.example.location_voiture.service.NotificationService;

import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import org.example.location_voiture.service.UserService;
import org.example.location_voiture.model.User;

@Controller
@RequestMapping("/paiements")
public class PaiementController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private org.example.location_voiture.service.StripeService stripeService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private FactureService factureService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/resume/{locationId}")
    public String paymentResume(@PathVariable Long locationId, Model model, Principal principal) {
        Location location = locationService.getLocationById(locationId);
        
        // Sécurité : Vérifier que c'est bien la location du client
        User user = userService.getUserByEmail(principal.getName());
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN && 
            (location.getClient() == null || !location.getClient().getEmail().equals(user.getEmail()))) {
            return "redirect:/locations?error=Acces+denie";
        }

        model.addAttribute("location", location);
        model.addAttribute("page", "paiements");
        return "paiements/resume";
    }

    @GetMapping
    public String listPaiements(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        if (user.getRole() == org.example.location_voiture.model.enums.Role.ADMIN) {
            model.addAttribute("paiements", paiementService.getAllPaiements());
        } else if (user.getClient() != null) {
            model.addAttribute("paiements", paiementService.getPaiementsByClient(user.getClient()));
            model.addAttribute("pendingLocations", locationService.getPendingPaymentsByClient(user.getClient()));
        } else {
            model.addAttribute("paiements", java.util.Collections.emptyList());
        }
        return "paiements/liste";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/effectuer")
    public String showAddForm(Model model) {
        model.addAttribute("paiement", new Paiement());
        model.addAttribute("locations", locationService.getAllLocations());
        return "paiements/effectuer";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/effectuer")
    public String addPaiement(@ModelAttribute Paiement paiement) {
        paiementService.savePaiement(paiement);
        return "redirect:/paiements";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("paiement", paiementService.getPaiementById(id));
        model.addAttribute("locations", locationService.getAllLocations());
        return "paiements/modifier";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/modifier/{id}")
    public String updatePaiement(@PathVariable Long id,
                                 @RequestParam Long locationId,
                                 @RequestParam Double montant,
                                 @RequestParam LocalDate datePaiement,
                                 @RequestParam String modePaiement,
                                 @RequestParam(required = false) String reference,
                                 @RequestParam String statut) {
        paiementService.updatePaiement(id, locationId, montant, datePaiement, modePaiement, reference, statut);
        return "redirect:/paiements";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/supprimer/{id}")
    public String deletePaiement(@PathVariable Long id) {
        paiementService.deletePaiement(id);
        return "redirect:/paiements";
    }

    @GetMapping("/{id}")
    public String detailsPaiement(@PathVariable Long id, Model model, Principal principal) {
        Paiement paiement = paiementService.getPaiementById(id);
        User user = userService.getUserByEmail(principal.getName());

        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN) {
            if (user.getClient() == null || !paiement.getLocation().getClient().getId().equals(user.getClient().getId())) {
                return "redirect:/access-denied";
            }
        }

        model.addAttribute("paiement", paiement);
        return "paiements/details";
    }

    @PostMapping("/process/{locationId}")
    public String processPaymentSelection(@PathVariable Long locationId, 
                                          @RequestParam("modePaiement") String modePaiement, 
                                          Principal principal, 
                                          org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if ("STRIPE".equalsIgnoreCase(modePaiement)) {
            return "redirect:/paiements/checkout/" + locationId;
        } else {
            // For Mobile Money or Cash, we redirect to instructions
            return "redirect:/paiements/instructions/" + locationId + "?mode=" + modePaiement;
        }
    }

    @GetMapping("/instructions/{locationId}")
    public String paymentInstructions(@PathVariable Long locationId, 
                                      @RequestParam("mode") String mode, 
                                      Model model, Principal principal) {
        Location location = locationService.getLocationById(locationId);
        User user = userService.getUserByEmail(principal.getName());
        
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN && 
            (location.getClient() == null || !location.getClient().getEmail().equals(user.getEmail()))) {
            return "redirect:/locations?error=Acces+denie";
        }
        
        // La location est déjà en EN_ATTENTE au moment de la réservation.
        // Elle restera en attente jusqu'à la validation du paiement manuel.

        model.addAttribute("location", location);
        model.addAttribute("mode", mode);
        return "paiements/instructions";
    }

    @PostMapping("/declarer/{locationId}")
    public String declarePaiementManuel(@PathVariable Long locationId, 
                                        @RequestParam("modePaiement") String modePaiement, 
                                        Principal principal, 
                                        org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        Location location = locationService.getLocationById(locationId);
        User user = userService.getUserByEmail(principal.getName());
        
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN && 
            (location.getClient() == null || !location.getClient().getEmail().equals(user.getEmail()))) {
            ra.addFlashAttribute("error", "Accès refusé.");
            return "redirect:/locations";
        }
        
        location.setPaiementDeclare(true);
        location.setModePaiementChoisi(modePaiement);
        locationService.saveLocation(location);
        
        // Notify Admins
        try {
            String clientName = (location.getClient() != null) ? location.getClient().getNomComplet() : "Client #" + location.getId();
            String modeStr = "MOBILE_MONEY".equals(modePaiement) ? "Mobile Money" : "Paiement sur place (Cash)";
            String notifMsg = clientName + " a déclaré vouloir payer la réservation #" + location.getId() + " par " + modeStr + ". Vérifiez et validez.";
            
            java.util.List<User> admins = userService.getAdmins();
            System.out.println("[Paiement DEBUG] Nombre d'admins trouvés pour la notification: " + admins.size());
            
            for (User admin : admins) {
                System.out.println("[Paiement DEBUG] Envoi de la notification à l'admin: " + admin.getEmail());
                notificationService.createNotification(
                    "Paiement Déclaré",
                    notifMsg,
                    "/locations/" + location.getId(), // Lien direct vers la location
                    admin
                );
            }
            System.out.println("[Paiement DEBUG] Notifications envoyées avec succès.");
        } catch (Exception e) {
            System.err.println("[Paiements] Erreur envoi notif: " + e.getMessage());
            e.printStackTrace();
        }

        ra.addFlashAttribute("success", "Votre déclaration a bien été enregistrée. Un administrateur va vérifier votre demande sous peu.");
        ra.addFlashAttribute("celebrate", true);
        return "redirect:/locations";
    }

    @GetMapping("/checkout/{locationId}")
    public String createCheckoutSession(@PathVariable Long locationId, Model model, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        System.out.println("[STRIPE] Début de création de session pour Location ID: " + locationId);
        try {
            org.example.location_voiture.model.Location location = locationService.getLocationById(locationId);
            User user = userService.getUserByEmail(principal.getName());
            
            if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN && 
                (user.getClient() == null || !location.getClient().getId().equals(user.getClient().getId()))) {
                System.out.println("[STRIPE] Accès refusé pour l'utilisateur: " + principal.getName());
                ra.addFlashAttribute("error", "Accès refusé.");
                return "redirect:/paiements";
            }
            
            long montantCard = location.getMontantTotal() != null ? location.getMontantTotal().longValue() : 0L;
            System.out.println("[STRIPE] Montant calculé: " + montantCard + " MGA");

            if (montantCard <= 0) {
                System.out.println("[STRIPE] Erreur: Montant invalide");
                ra.addFlashAttribute("error", "Montant invalide.");
                return "redirect:/paiements";
            }

            String successUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/paiements/success")
                                .queryParam("locationId", locationId)
                                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                                .build().toUriString();
                                
            String cancelUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/paiements/cancel").build().toUriString();

            System.out.println("[STRIPE] Success URL: " + successUrl);

            com.stripe.model.checkout.Session session = stripeService.createCheckoutSession(successUrl, cancelUrl, "Location de véhicule (Réf: " + locationId + ")", montantCard);

            System.out.println("[STRIPE] Session créée avec succès! Redirection vers: " + session.getUrl());
            return "redirect:" + session.getUrl();
            
        } catch (Exception e) {
            System.out.println("[STRIPE] !!! ERREUR CRITIQUE: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Erreur Stripe : " + e.getMessage());
            return "redirect:/paiements?stripeError=true";
        }
    }

    @GetMapping("/success")
    public String checkoutSuccess(@RequestParam("locationId") Long locationId, 
                                  @RequestParam("session_id") String sessionId,
                                  org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            org.example.location_voiture.model.Location location = locationService.getLocationById(locationId);
            
            Paiement paiement = new Paiement();
            paiement.setLocation(location);
            paiement.setMontant(location.getMontantTotal());
            paiement.setDatePaiement(LocalDate.now());
            paiement.setModePaiement("Stripe Card");
            paiement.setReference(sessionId);
            paiement.setStatut(org.example.location_voiture.model.enums.StatutPaiement.EFFECTUE);
            
            Paiement saved = paiementService.savePaiement(paiement);

            // Mettre à jour le statut de la location en PAYEE
            location.setStatut(org.example.location_voiture.model.enums.StatutLocation.PAYEE);
            locationService.saveLocation(location);

            // Création ou Mise à jour automatique de la facture
            try {
                Facture facture = factureService.getFactureByLocation(locationId);
                // Si la facture n'existe pas encore (ce qui est souvent le cas), on la génère
                if (facture == null) {
                    facture = factureService.generateFactureForLocation(locationId, org.example.location_voiture.model.enums.Currency.MGA, null);
                }

                if (facture != null) {
                    facture.setStatut(StatutFacture.PAYEE);
                    factureService.saveFacture(facture);
                    System.out.println("[STRIPE] Facture #" + facture.getNumeroFacture() + " générée/mise à jour en PAYÉE");
                    
                    // Envoi d'un email de confirmation avec la facture PDF
                    if (location.getClient() != null && location.getClient().getEmail() != null) {
                        try {
                            byte[] pdfBytes = pdfService.generateInvoicePdfBytes(facture);
                            String body = "Bonjour " + location.getClient().getPrenom() + ",\n\n" +
                                          "Nous avons bien reçu votre paiement pour la réservation #" + locationId + ".\n" +
                                          "Veuillez trouver ci-joint votre facture acquittée.\n\n" +
                                          "Merci de votre confiance,\nL'équipe AutoRent";
                            
                            emailService.sendEmailWithAttachment(location.getClient().getEmail(),
                                    "Confirmation de Paiement - Réservation #" + locationId,
                                    body,
                                    pdfBytes,
                                    "facture_" + facture.getNumeroFacture() + ".pdf");
                        } catch (Exception ex) {
                            System.err.println("[STRIPE] Erreur envoi email: " + ex.getMessage());
                        }
                    }
                    
                    // Notification pour les Admins
                    try {
                        String clientName = (location.getClient() != null) ? location.getClient().getNom() : "Client #" + location.getId();
                        String notifMsg = "Nouveau paiement de " + String.format("%.0f", saved.getMontant()) + " Ar reçu de " + clientName;
                        List<User> admins = userService.getAdmins();
                        for (User admin : admins) {
                            notificationService.createNotification(
                                "Nouveau Paiement",
                                notifMsg,
                                "/paiements/" + saved.getId(),
                                admin
                            );
                        }
                    } catch (Exception ne) {
                        System.err.println("[STRIPE] Erreur creation notification: " + ne.getMessage());
                    }
                }
            } catch (Exception fe) {
                System.out.println("[STRIPE] Erreur lors de la génération/mise à jour de la facture: " + fe.getMessage());
            }
            
            ra.addFlashAttribute("success", "Paiement réussi avec Stripe ! Votre transaction a été enregistrée.");
            ra.addFlashAttribute("celebrate", true); // Flag for confetti
            return "redirect:/paiements/" + saved.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de l'enregistrement du paiement.");
            return "redirect:/locations/" + locationId;
        }
    }

    @GetMapping("/cancel")
    public String checkoutCancel(org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        ra.addFlashAttribute("warning", "Le paiement en ligne a été annulé.");
        return "redirect:/paiements";
    }
}