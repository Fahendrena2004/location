package org.example.location_voiture.service;

import org.example.location_voiture.model.Paiement;
import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.enums.StatutPaiement;
import org.example.location_voiture.repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private NotificationService notificationService;

    public List<Paiement> getAllPaiements() {
        return paiementRepository.findAll();
    }

    public List<Paiement> getPaiementsByClient(org.example.location_voiture.model.Client client) {
        return paiementRepository.findByLocationClient(client);
    }

    public Paiement getPaiementById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'id: " + id));
    }

    public Paiement savePaiement(Paiement paiement) {
        Paiement saved = paiementRepository.save(paiement);
        if (saved.getStatut() == org.example.location_voiture.model.enums.StatutPaiement.EFFECTUE) {
            if (saved.getLocation() != null && saved.getLocation().getClient() != null && saved.getLocation().getClient().getUtilisateur() != null) {
                notificationService.createNotification(
                    "Paiement Reçu",
                    "Votre paiement de " + saved.getMontant() + " Ar a été validé pour la location #" + saved.getLocation().getId() + ".",
                    "/paiements",
                    saved.getLocation().getClient().getUtilisateur()
                );
            }
        }
        return saved;
    }

    public Paiement updatePaiement(Long id, Long locationId, Double montant, LocalDate datePaiement,
                                    String modePaiement, String reference, String statut) {
        Paiement paiement = getPaiementById(id);
        if (locationId != null) {
            paiement.setLocation(locationService.getLocationById(locationId));
        }
        paiement.setMontant(montant);
        paiement.setDatePaiement(datePaiement);
        paiement.setModePaiement(modePaiement);
        paiement.setReference(reference);
        if (statut != null && !statut.isBlank()) {
            paiement.setStatut(StatutPaiement.valueOf(statut));
        }
        return paiementRepository.save(paiement);
    }

    public void deletePaiement(Long id) {
        Paiement paiement = getPaiementById(id);
        paiementRepository.delete(paiement);
    }

    public List<Paiement> getPaiementsByLocation(Long locationId) {
        Location location = locationService.getLocationById(locationId);
        return paiementRepository.findByLocation(location);
    }

    public Double calculerRevenuTotal() {
        Double total = paiementRepository.sumAllMontants();
        return total != null ? total : 0.0;
    }

    public Double calculerRevenuMois() {
        Double total = paiementRepository.sumMontantsMoisCourant();
        return total != null ? total : 0.0;
    }

    public Double calculerRevenuMoisPrecedent() {
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate start = now.minusMonths(1).withDayOfMonth(1);
        java.time.LocalDate end = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
        Double total = paiementRepository.sumMontantsBetween(start, end);
        return total != null ? total : 0.0;
    }

    public Double getEvolutionRevenuMois() {
        Double moisCourant = calculerRevenuMois();
        Double moisPrecedent = calculerRevenuMoisPrecedent();
        if (moisPrecedent == 0) {
            return moisCourant > 0 ? 100.0 : 0.0;
        }
        return ((moisCourant - moisPrecedent) / moisPrecedent) * 100.0;
    }

    public List<Double> getRevenusParMois() {
        List<Object[]> results = paiementRepository.sumRevenusParMois();
        List<Double> revenusParMois = new java.util.ArrayList<>(12);
        for (int i = 0; i < 12; i++) revenusParMois.add(0.0);

        for (Object[] result : results) {
            int mois = ((Number) result[0]).intValue() - 1;
            Double total = ((Number) result[1]).doubleValue();
            revenusParMois.set(mois, total);
        }
        return revenusParMois;
    }

    public java.util.Map<String, Double> getRevenusParTypeClient() {
        List<Object[]> results = paiementRepository.sumRevenusParTypeClient();
        java.util.Map<String, Double> mapping = new java.util.HashMap<>();
        for (Object[] result : results) {
            mapping.put((String) result[0], (Double) result[1]);
        }
        return mapping;
    }
}