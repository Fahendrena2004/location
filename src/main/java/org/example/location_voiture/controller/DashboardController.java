package org.example.location_voiture.controller;

import org.example.location_voiture.repository.EntretienRepository;
import org.example.location_voiture.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Controller
public class DashboardController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private UserService userService;

    @Autowired
    private EntretienRepository entretienRepository;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model, java.security.Principal principal) {
        org.example.location_voiture.model.User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        
        if (user.getRole() == org.example.location_voiture.model.enums.Role.ADMIN) {
            model.addAttribute("totalClients", clientService.countClients());
            model.addAttribute("totalVoitures", voitureService.countVoitures());
            model.addAttribute("voituresDisponibles", voitureService.countDisponibles());
            model.addAttribute("voituresLouees", voitureService.countLouees());
            model.addAttribute("locationsEnCours", locationService.countEnCours());
            model.addAttribute("totalLocations", locationService.countAll());
            model.addAttribute("revenuTotal", paiementService.calculerRevenuTotal());
            model.addAttribute("revenuMois", paiementService.calculerRevenuMois());
            model.addAttribute("evolutionRevenus", paiementService.getEvolutionRevenuMois());
            model.addAttribute("revenusParMois", paiementService.getRevenusParMois());
            model.addAttribute("revenusParTypeClient", paiementService.getRevenusParTypeClient());
            model.addAttribute("locationsTerminees", locationService.countTerminees());
            model.addAttribute("locationsAnnulees", locationService.countAnnulees());
            model.addAttribute("locationsParMois", locationService.getLocationsParMois());
            model.addAttribute("popularVoitures", locationService.getPopularVoitures(5));
            model.addAttribute("topClients", locationService.getTopSpendingClients(5));
            model.addAttribute("dernieresLocations", locationService.getDernieresLocations(5));
            model.addAttribute("locationsEnAttente", locationService.getLocationsEnAttente());
            model.addAttribute("locationsEnRetard", locationService.getLocationsEnRetard());
            model.addAttribute("alertesMaintenance", entretienRepository.findUpcomingAlerts(LocalDate.now().plusDays(7)));
            return "dashboard/index";
        } else {
            // Vue Client
            org.example.location_voiture.model.Client client = user.getClient();
            if (client != null) {
                model.addAttribute("client", client);
                
                // Load via service to stay within the transaction and avoid LazyInitializationException
                java.util.List<org.example.location_voiture.model.Location> locations =
                        locationService.getLocationsByClient(client);
                if (locations == null) {
                    locations = new java.util.ArrayList<>();
                }
                
                // Force-initialize lazy collections while still in the transaction
                for (org.example.location_voiture.model.Location loc : locations) {
                    if (loc.getVoitures() != null) loc.getVoitures().size();
                    if (loc.getChauffeurs() != null) loc.getChauffeurs().size();
                    try { if (loc.getFacture() != null) loc.getFacture().getId(); } catch (Exception ignored) {}
                }
                
                model.addAttribute("mesLocations", locations);
                
                double totalDepense = locations.stream()
                        .mapToDouble(l -> l.getMontantTotal() != null ? l.getMontantTotal() : 0.0)
                        .sum();
                model.addAttribute("totalDepense", totalDepense);
                
                long locationsActives = locations.stream()
                        .filter(l -> l.getStatut() == org.example.location_voiture.model.enums.StatutLocation.EN_COURS)
                        .count();
                model.addAttribute("locationsActives", locationsActives);
            } else {
                model.addAttribute("mesLocations", new java.util.ArrayList<>());
                model.addAttribute("totalDepense", 0.0);
                model.addAttribute("locationsActives", 0L);
            }
            return "dashboard/client";
        }
    }
}