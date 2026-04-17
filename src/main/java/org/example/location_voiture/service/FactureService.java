package org.example.location_voiture.service;

import org.example.location_voiture.model.Facture;
import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.enums.StatutFacture;
import org.example.location_voiture.repository.FactureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class FactureService {

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private NotificationService notificationService;

    public List<Facture> getAllFactures() {
        return factureRepository.findAll();
    }

    public List<Facture> getFacturesByClient(org.example.location_voiture.model.Client client) {
        return factureRepository.findByLocationClient(client);
    }

    public Facture getFactureById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + id));
    }

    public Facture saveFacture(Facture facture) {
        return factureRepository.save(facture);
    }

    public Facture updateFacture(Long id, Facture factureDetails) {
        Facture facture = getFactureById(id);
        facture.setNumeroFacture(factureDetails.getNumeroFacture());
        facture.setDateEmission(factureDetails.getDateEmission());
        facture.setMontantHT(factureDetails.getMontantHT());
        facture.setTva(factureDetails.getTva());
        facture.setMontantTTC(factureDetails.getMontantTTC());
        facture.setStatut(factureDetails.getStatut());
        return factureRepository.save(facture);
    }

    public void deleteFacture(Long id) {
        Facture facture = getFactureById(id);
        factureRepository.delete(facture);
    }


    public Facture generateFactureForLocation(Long locationId, org.example.location_voiture.model.enums.Currency devise, Long compteId) {
        Location location = locationService.getLocationById(locationId);

        Facture facture = new Facture();
        facture.setLocation(location);
        facture.setDevise(devise);



        String numero = "FACT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + new Random().nextInt(1000);
        facture.setNumeroFacture(numero);
        facture.setDateEmission(LocalDate.now());

        double montantBaseMGA = location.getMontantTotal();
        double penalite = 0.0;

        // Si la location est terminée en retard, on utilise la date de retour effective
        // Sinon s'il est EN_COURS, on calcule par rapport à aujourd'hui
        LocalDate dateFin = location.getDateFin();
        LocalDate today = LocalDate.now();
        LocalDate dateEffective = location.getDateRetourEffective();
        
        if (dateEffective != null && dateEffective.isAfter(dateFin)) {
            long joursRetard = java.time.temporal.ChronoUnit.DAYS.between(dateFin, dateEffective);
            double tarifJournalierTotal = 0;
            if (location.getVoitures() != null) {
                for (org.example.location_voiture.model.Voiture v : location.getVoitures()) {
                    tarifJournalierTotal += (v.getPrixParJour() != null ? v.getPrixParJour() : 0);
                }
            }
            penalite = joursRetard * tarifJournalierTotal * 1.5;
        } else if (dateEffective == null && today.isAfter(dateFin) && location.getStatut() == org.example.location_voiture.model.enums.StatutLocation.EN_COURS) {
            long joursRetard = java.time.temporal.ChronoUnit.DAYS.between(dateFin, today);
            double tarifJournalierTotal = 0;
            if (location.getVoitures() != null) {
                for (org.example.location_voiture.model.Voiture v : location.getVoitures()) {
                    tarifJournalierTotal += (v.getPrixParJour() != null ? v.getPrixParJour() : 0);
                }
            }
            penalite = joursRetard * tarifJournalierTotal * 1.5; // Taux de 50% en plus
        }

        double taux = 1.0;
        facture.setTauxChangeApplique(taux);
        
        double montantHT = (montantBaseMGA + penalite) * taux;
        double tva = 0.0; // TVA Supprimée (0%)

        facture.setPenalite(penalite * taux);
        facture.setMontantHT(montantHT);
        facture.setTva(tva);
        facture.setMontantTTC(montantHT); // TTC = HT
        facture.setStatut(StatutFacture.EN_ATTENTE);

        Facture saved = factureRepository.save(facture);
        
        if (saved.getLocation().getClient() != null && saved.getLocation().getClient().getUtilisateur() != null) {
            notificationService.createNotification(
                "Nouvelle Facture",
                "Une facture (#" + saved.getNumeroFacture() + ") a été générée pour votre location #" + saved.getLocation().getId() + ".",
                "/factures",
                saved.getLocation().getClient().getUtilisateur()
            );
        }

        return saved;
    }

    public Facture getFactureByLocation(Long locationId) {
        Location location = locationService.getLocationById(locationId);
        return factureRepository.findByLocation(location);
    }

    public Facture getFactureByNumero(String numeroFacture) {
        return factureRepository.findByNumeroFacture(numeroFacture);
    }
}