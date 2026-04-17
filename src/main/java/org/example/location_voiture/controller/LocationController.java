package org.example.location_voiture.controller;

import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.enums.StatutLocation;
import org.example.location_voiture.service.ClientService;
import org.example.location_voiture.service.ContratPdfGenerator;
import org.example.location_voiture.service.LocationService;
import org.example.location_voiture.service.VoitureService;
import org.example.location_voiture.service.ChauffeurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.FileCopyUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import org.example.location_voiture.service.UserService;
import org.example.location_voiture.model.User;

@Controller
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private UserService userService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ChauffeurService chauffeurService;

    @Autowired
    private ContratPdfGenerator contratPdfGenerator;

    @Autowired
    private org.example.location_voiture.service.EmailService emailService;

    @GetMapping
    public String listLocations(@RequestParam(required = false) String query,
                                @RequestParam(required = false) String statut,
                                @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<Location> locations;
        
        if (user.getRole() == org.example.location_voiture.model.enums.Role.ADMIN) {
            locations = locationService.searchLocations(query, statut, dateDebut, dateFin);
        } else if (user.getClient() != null) {
            locations = locationService.getLocationsByClient(user.getClient());
        } else {
            locations = java.util.Collections.emptyList();
        }

        model.addAttribute("locations", locations);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("page", "locations");
        model.addAttribute("query", query);
        model.addAttribute("statut", statut);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("allStatuts", StatutLocation.values());
        return "locations/liste";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public void exportLocations(@RequestParam(required = false) Integer mois,
                                @RequestParam(required = false) Integer annee,
                                @RequestParam(defaultValue = "csv") String format,
                                HttpServletResponse response) throws IOException {
        
        List<Location> locations = locationService.getAllLocations();
        
        if (mois != null && annee != null) {
            locations = locations.stream()
                .filter(l -> (l.getDateDebut() != null && l.getDateDebut().getMonthValue() == mois && l.getDateDebut().getYear() == annee) ||
                             (l.getDateFin() != null && l.getDateFin().getMonthValue() == mois && l.getDateFin().getYear() == annee))
                .collect(Collectors.toList());
        }

        if ("pdf".equalsIgnoreCase(format)) {
            exportToPdf(locations, mois, annee, response);
        } else {
            exportToCsv(locations, mois, annee, response);
        }
    }

    private void exportToCsv(List<Location> locations, Integer mois, Integer annee, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        String filename = "locations_export_" + (mois != null ? mois + "_" + annee : "all") + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("ID;Client;Voitures Louées;Date Début;Date Fin;Montant Total (Ar);Statut");
        
        for (Location l : locations) {
            String clientName = l.getClient() != null ? l.getClient().getNom() + " " + l.getClient().getPrenom() : "N/A";
            String voitures = l.getVoitures() != null ? l.getVoitures().stream()
                .map(v -> v.getMarque() + " " + v.getModele() + " (" + v.getPlaqueImmatriculation() + ")")
                .collect(Collectors.joining(", ")) : "";
            
            writer.printf("%d;\"%s\";\"%s\";%s;%s;%.2f;%s\n", 
                l.getId(), clientName, voitures, l.getDateDebut(), l.getDateFin(), 
                l.getMontantTotal() != null ? l.getMontantTotal() : 0.0, l.getStatut());
        }
    }

    private void exportToPdf(List<Location> locations, Integer mois, Integer annee, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String filename = "locations_export_" + (mois != null ? mois + "_" + annee : "all") + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);

        Paragraph title = new Paragraph("Rapport de Location - " + (mois != null ? mois + "/" + annee : "Global"), fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Spacer

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {1.0f, 3.0f, 3.5f, 2.0f, 2.0f, 2.5f});
        table.setSpacingBefore(10);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        cell.setPadding(5);

        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontHeader.setSize(10);

        String[] headers = {"ID", "Client", "Véhicules", "Début", "Fin", "Montant (Ar)"};
        for (String header : headers) {
            cell.setPhrase(new Phrase(header, fontHeader));
            table.addCell(cell);
        }

        Font fontRow = FontFactory.getFont(FontFactory.HELVETICA);
        fontRow.setSize(9);

        for (Location l : locations) {
            table.addCell(new Phrase(String.valueOf(l.getId()), fontRow));
            table.addCell(new Phrase(l.getClient() != null ? l.getClient().getNom() + " " + l.getClient().getPrenom() : "N/A", fontRow));
            String voitures = l.getVoitures() != null ? l.getVoitures().stream()
                .map(v -> v.getMarque() + " " + v.getModele())
                .collect(Collectors.joining(", ")) : "";
            table.addCell(new Phrase(voitures, fontRow));
            table.addCell(new Phrase(String.valueOf(l.getDateDebut()), fontRow));
            table.addCell(new Phrase(String.valueOf(l.getDateFin()), fontRow));
            table.addCell(new Phrase(String.format("%,.0f", l.getMontantTotal()), fontRow));
        }

        document.add(table);
        document.close();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ajouter")
    public String showAddForm(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("voitures", voitureService.getVoituresDisponibles());
        model.addAttribute("chauffeurs", chauffeurService.getChauffeursDisponibles());
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("today", LocalDate.now());
        return "locations/ajouter";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ajouter")
    public String addLocation(@ModelAttribute Location location, Model model) {
        try {
            locationService.saveLocation(location);
            return "redirect:/locations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("location", location);
            model.addAttribute("voitures", voitureService.getVoituresDisponibles());
            model.addAttribute("chauffeurs", chauffeurService.getChauffeursDisponibles());
            model.addAttribute("clients", clientService.getAllClients());
            model.addAttribute("today", LocalDate.now());
            return "locations/ajouter";
        }
    }

    /** Flux spécifique pour la réservation Client **/
    @GetMapping("/reserver")
    public String showReservationForm(@RequestParam(required = false) Long voitureId, Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        if (user.getClient() == null) {
            return "redirect:/dashboard";
        }
        
        Location location = new Location();
        location.setClient(user.getClient());
        if (voitureId != null) {
            java.util.List<org.example.location_voiture.model.Voiture> voitures = new java.util.ArrayList<>();
            voitures.add(voitureService.getVoitureById(voitureId));
            location.setVoitures(voitures);
        }
        
        model.addAttribute("location", location);
        model.addAttribute("voitures", voitureService.getVoituresDisponibles());
        model.addAttribute("chauffeurs", chauffeurService.getChauffeursDisponibles());
        model.addAttribute("today", LocalDate.now());
        return "locations/reserver";
    }

    @PostMapping("/reserver")
    public String processReservation(@ModelAttribute Location location, Model model, Principal principal, RedirectAttributes ra) {
        User user = userService.getUserByEmail(principal.getName());
        try {
            location.setClient(user.getClient());
            locationService.saveLocation(location);
            ra.addFlashAttribute("success", "Votre demande de réservation a été envoyée. Un administrateur l'examinera bientôt.");
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("location", location);
            model.addAttribute("voitures", voitureService.getVoituresDisponibles());
            model.addAttribute("chauffeurs", chauffeurService.getChauffeursDisponibles());
            model.addAttribute("today", LocalDate.now());
            return "locations/reserver";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Location location = locationService.getLocationById(id);
        
        // Filter available cars + assigned ones
        java.util.List<org.example.location_voiture.model.Voiture> allAvailVoitures = new java.util.ArrayList<>(voitureService.getVoituresDisponibles());
        if (location.getVoitures() != null) {
            for (org.example.location_voiture.model.Voiture v : location.getVoitures()) {
                if (allAvailVoitures.stream().noneMatch(av -> av.getId().equals(v.getId()))) {
                    allAvailVoitures.add(v);
                }
            }
        }

        // Filter available chauffeurs + assigned ones
        java.util.List<org.example.location_voiture.model.Chauffeur> allAvailChauffeurs = new java.util.ArrayList<>(chauffeurService.getChauffeursDisponibles());
        if (location.getChauffeurs() != null) {
            for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                if (allAvailChauffeurs.stream().noneMatch(ac -> ac.getId().equals(c.getId()))) {
                    allAvailChauffeurs.add(c);
                }
            }
        }

        model.addAttribute("location", location);
        model.addAttribute("voitures", allAvailVoitures);
        model.addAttribute("chauffeurs", allAvailChauffeurs);
        model.addAttribute("clients", clientService.getAllClients());
        return "locations/modifier";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/modifier/{id}")
    public String updateLocation(@PathVariable Long id, @ModelAttribute Location location, Model model) {
        try {
            locationService.updateLocation(id, location);
            return "redirect:/locations";
        } catch (RuntimeException e) {
            Location original = locationService.getLocationById(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("location", location);
            
            // Re-filter for error view
            java.util.List<org.example.location_voiture.model.Voiture> allAvailVoitures = new java.util.ArrayList<>(voitureService.getVoituresDisponibles());
            if (original.getVoitures() != null) {
                for (org.example.location_voiture.model.Voiture v : original.getVoitures()) {
                    if (allAvailVoitures.stream().noneMatch(av -> av.getId().equals(v.getId()))) {
                        allAvailVoitures.add(v);
                    }
                }
            }
            java.util.List<org.example.location_voiture.model.Chauffeur> allAvailChauffeurs = new java.util.ArrayList<>(chauffeurService.getChauffeursDisponibles());
            if (original.getChauffeurs() != null) {
                for (org.example.location_voiture.model.Chauffeur c : original.getChauffeurs()) {
                    if (allAvailChauffeurs.stream().noneMatch(ac -> ac.getId().equals(c.getId()))) {
                        allAvailChauffeurs.add(c);
                    }
                }
            }
            
            model.addAttribute("voitures", allAvailVoitures);
            model.addAttribute("chauffeurs", allAvailChauffeurs);
            model.addAttribute("clients", clientService.getAllClients());
            return "locations/modifier";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/supprimer/{id}")
    public String deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return "redirect:/locations";
    }

    /** Client: Annuler sa propre réservation EN_ATTENTE **/
    @GetMapping("/annuler/{id}")
    public String annulerLocation(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        Location location = locationService.getLocationById(id);
        User user = userService.getUserByEmail(principal.getName());
        
        // Sécurité: Vérifier le propriétaire
        if (user.getClient() == null || !location.getClient().getId().equals(user.getClient().getId())) {
            ra.addFlashAttribute("error", "Vous n'êtes pas autorisé à annuler cette réservation.");
            return "redirect:/locations";
        }
        
        // Sécurité: Vérifier le statut
        if (location.getStatut() != StatutLocation.EN_ATTENTE) {
            ra.addFlashAttribute("error", "Seules les réservations en attente peuvent être annulées.");
            return "redirect:/locations";
        }
        
        try {
            location.setStatut(StatutLocation.ANNULEE);
            locationService.saveLocation(location);
            ra.addFlashAttribute("success", "Votre réservation a été annulée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de l'annulation : " + e.getMessage());
        }
        return "redirect:/locations";
    }

    @GetMapping("/{id}")
    public String detailsLocation(@PathVariable Long id, Model model, Principal principal) {
        Location location = locationService.getLocationById(id);
        User user = userService.getUserByEmail(principal.getName());
        
        if (user.getRole() != org.example.location_voiture.model.enums.Role.ADMIN) {
            if (user.getClient() == null || !location.getClient().getId().equals(user.getClient().getId())) {
                return "redirect:/access-denied"; // Or some error
            }
        }

        model.addAttribute("location", location);
        return "locations/details";
    }

    @GetMapping("/calendrier")
    public String calendrier(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        if (user.getRole() == org.example.location_voiture.model.enums.Role.ADMIN) {
            model.addAttribute("locations", locationService.getAllLocations());
        } else if (user.getClient() != null) {
            model.addAttribute("locations", locationService.getLocationsByClient(user.getClient()));
        } else {
            model.addAttribute("locations", java.util.Collections.emptyList());
        }
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("page", "calendrier");
        return "locations/calendrier";
    }

    /** Admin: Approuver une location EN_ATTENTE **/
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approuver")
    public String approuverLocation(@PathVariable Long id, RedirectAttributes ra) {
        try {
            locationService.approuverLocation(id);
            ra.addFlashAttribute("success", "Location approuvée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/locations";
    }

    /** Admin: Rejeter une location EN_ATTENTE **/
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/rejeter")
    public String rejeterLocation(@PathVariable Long id, RedirectAttributes ra) {
        try {
            locationService.rejeterLocation(id);
            ra.addFlashAttribute("success", "Location rejetée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/locations";
    }

    /** Admin: Terminer une location avec date de retour réelle (et calcul pénalité) **/
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/terminer")
    public String terminerLocation(@PathVariable Long id,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRetour,
                                   RedirectAttributes ra) {
        System.out.println("[DEBUG] Appel de terminerLocation pour ID: " + id + " avec dateRetour: " + dateRetour);
        try {
            locationService.terminerLocation(id, dateRetour);
            ra.addFlashAttribute("success", "Location terminée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/locations";
    }

    @GetMapping("/{id}/contrat")
    public void imprimerContrat(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Location location = locationService.getLocationById(id);
        
        response.setContentType("application/pdf");
        String filename = "contrat_location_" + id + ".pdf";
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        java.io.ByteArrayInputStream bis = contratPdfGenerator.generateContratPdf(location);
        FileCopyUtils.copy(bis, response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping("/{id}/signer")
    public String signerContrat(@PathVariable Long id, @RequestParam("signatureData") String signatureData, RedirectAttributes ra) {
        try {
            Location location = locationService.getLocationById(id);
            if (location != null) {
                location.setSignatureClient(signatureData);
                locationService.saveLocation(location);
                
                // Envoyer un email avec le contrat signé
                if (location.getClient() != null && location.getClient().getEmail() != null) {
                    try {
                        byte[] pdfBytes = contratPdfGenerator.generateContratPdf(location).readAllBytes();
                        emailService.sendEmailWithAttachment(location.getClient().getEmail(),
                                "Votre Contrat de Location - Signé",
                                "Bonjour " + location.getClient().getPrenom() + ",\n\nVeuillez trouver en pièce jointe votre contrat de location signé.\n\nCordialement,\nL'équipe",
                                pdfBytes,
                                "contrat_location_" + id + ".pdf");
                    } catch (Exception ex) {
                        System.err.println("Erreur email: " + ex.getMessage());
                    }
                }
                
                ra.addFlashAttribute("success", "Le contrat a été signé avec succès et envoyé par email.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la signature : " + e.getMessage());
        }
        return "redirect:/locations/" + id;
    }
}