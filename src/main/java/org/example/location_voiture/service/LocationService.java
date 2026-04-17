package org.example.location_voiture.service;

import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.model.enums.StatutLocation;
import org.example.location_voiture.model.enums.StatutVoiture;
import org.example.location_voiture.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public List<Location> getLocationsByClient(org.example.location_voiture.model.Client client) {
        return locationRepository.findByClient(client);
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trouvée avec l'id: " + id));
    }

    @Transactional
    public Location saveLocation(Location location) {
        // 1. Vérification chevauchement de dates pour chaque voiture
        if (location.getVoitures() != null && location.getDateDebut() != null && location.getDateFin() != null) {
            Long excludeId = (location.getId() != null) ? location.getId() : -1L;
            for (Voiture v : location.getVoitures()) {
                boolean overlap = locationRepository.existsOverlap(
                        v.getId(), location.getDateDebut(), location.getDateFin(), excludeId);
                if (overlap) {
                    throw new RuntimeException("La voiture " + v.getMarque() + " " + v.getModele()
                            + " est déjà réservée pour cette période.");
                }
                // Vérifier le statut global de la voiture (ENTRETIEN)
                if (v.getStatut() == StatutVoiture.MAINTENANCE) {
                    throw new RuntimeException("La voiture " + v.getMarque() + " " + v.getModele()
                            + " est en entretien et ne peut pas être réservée.");
                }
            }
        }

        // 2. Vérifier la disponibilité des chauffeurs
        if (location.getChauffeurs() != null) {
            Location oldLocation = null;
            if (location.getId() != null) {
                oldLocation = locationRepository.findById(location.getId()).orElse(null);
            }
            for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                boolean alreadyAssigned = false;
                if (oldLocation != null && oldLocation.getChauffeurs() != null) {
                    final Long currId = c.getId();
                    alreadyAssigned = oldLocation.getChauffeurs().stream().anyMatch(oldC -> oldC.getId().equals(currId));
                }
                if (!c.isDisponible() && !alreadyAssigned) {
                    throw new RuntimeException("Le chauffeur " + c.getNom() + " n'est pas disponible.");
                }
            }
        }

        // 3. Calcul du montant total
        location.setMontantTotal(calculerMontantTotal(location));

        // 4. Statut initial = EN_ATTENTE (uniquement pour les nouvelles réservations)
        if (location.getId() == null || location.getStatut() == null) {
            location.setStatut(StatutLocation.EN_ATTENTE);
        }

        boolean isNew = location.getId() == null;
        Location saved = locationRepository.save(location);

        if (isNew) {
            String clientName = (saved.getClient() != null) ? saved.getClient().getNom() + " " + saved.getClient().getPrenom() : "Inconnu";
            // Notifier tous les administrateurs
            java.util.List<org.example.location_voiture.model.User> admins = userService.getAdmins();
            for (org.example.location_voiture.model.User admin : admins) {
                notificationService.createNotification(
                    "Nouvelle Réservation",
                    "Une nouvelle réservation (#" + saved.getId() + ") a été effectuée par " + clientName + ".",
                    "/locations/" + saved.getId(),
                    admin
                );
            }
        }

        return saved;
    }

    /**
     * Approuver une location (Admin) : passer EN_ATTENTE → EN_COURS
     * et marquer voitures + chauffeurs comme occupés.
     */
    @Transactional
    public Location approuverLocation(Long id) {
        Location location = getLocationById(id);
        if (location.getStatut() == StatutLocation.EN_COURS) {
            return location; // Déjà approuvée
        }
        if (location.getStatut() != StatutLocation.EN_ATTENTE) {
            throw new RuntimeException("Seules les locations EN_ATTENTE peuvent être approuvées (Statut actuel: " + location.getStatut() + ")");
        }

        // Marquer voitures comme louées
        if (location.getVoitures() != null) {
            for (Voiture v : location.getVoitures()) {
                voitureService.updateStatut(v.getId(), StatutVoiture.LOUE);
            }
        }
        // Marquer chauffeurs comme indisponibles
        if (location.getChauffeurs() != null) {
            for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                c.setDisponible(false);
            }
        }

        location.setStatut(StatutLocation.EN_COURS);
        Location saved = locationRepository.save(location);

        // Notifier le client
        if (saved.getClient() != null && saved.getClient().getUtilisateur() != null) {
            notificationService.createNotification(
                "Réservation Confirmée",
                "Votre demande de réservation #" + saved.getId() + " a été acceptée.",
                "/locations/" + saved.getId(),
                saved.getClient().getUtilisateur()
            );
        }

        return saved;
    }

    /**
     * Rejeter une location (Admin) : passer EN_ATTENTE → ANNULEE
     */
    @Transactional
    public Location rejeterLocation(Long id) {
        Location location = getLocationById(id);
        if (location.getStatut() == StatutLocation.ANNULEE) {
            return location; // Déjà rejetée
        }
        if (location.getStatut() != StatutLocation.EN_ATTENTE) {
            throw new RuntimeException("Seules les locations EN_ATTENTE peuvent être rejetées (Statut actuel: " + location.getStatut() + ")");
        }
        location.setStatut(StatutLocation.ANNULEE);
        Location saved = locationRepository.save(location);

        // Notifier le client
        if (saved.getClient() != null && saved.getClient().getUtilisateur() != null) {
            notificationService.createNotification(
                "Réservation Refusée",
                "Votre demande de réservation #" + saved.getId() + " a été rejetée.",
                "/locations/" + saved.getId(),
                saved.getClient().getUtilisateur()
            );
        }

        return saved;
    }

    /**
     * Terminer une location with calcul des pénalités si retard.
     * penaltyRate = taux multiplicateur journalier en cas de dépassement (ex: 1.5 = +50%)
     */
    @Transactional
    public Location terminerLocation(Long id, LocalDate dateRetourReelle) {
        Location location = getLocationById(id);
        if (location.getStatut() != StatutLocation.EN_COURS) {
            throw new RuntimeException("Seules les locations EN_COURS peuvent être terminées.");
        }

        location.setDateRetourEffective(dateRetourReelle != null ? dateRetourReelle : LocalDate.now());
        double montantFinal = location.getMontantTotal();

        // Calcul pénalité si retour tardif
        if (dateRetourReelle != null && dateRetourReelle.isAfter(location.getDateFin())) {
            long joursRetard = ChronoUnit.DAYS.between(location.getDateFin(), dateRetourReelle);
            double tarifJournalierTotal = 0;
            if (location.getVoitures() != null) {
                for (Voiture v : location.getVoitures()) {
                    tarifJournalierTotal += (v.getPrixParJour() != null ? v.getPrixParJour() : 0);
                }
            }
            double penalite = joursRetard * tarifJournalierTotal * 1.5; // pénalité 50%
            montantFinal += penalite;
        }

        location.setMontantTotal(montantFinal);
        location.setStatut(StatutLocation.TERMINEE);

        // Libérer voitures et chauffeurs
        if (location.getVoitures() != null) {
            for (Voiture v : location.getVoitures()) {
                voitureService.updateStatut(v.getId(), StatutVoiture.DISPONIBLE);
            }
        }
        if (location.getChauffeurs() != null) {
            for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                c.setDisponible(true);
            }
        }

        return locationRepository.save(location);
    }

    public Location updateLocation(Long id, Location locationDetails) {
        Location location = getLocationById(id);
        location.setDateDebut(locationDetails.getDateDebut());
        location.setDateFin(locationDetails.getDateFin());
        location.setVoitures(locationDetails.getVoitures());
        location.setChauffeurs(locationDetails.getChauffeurs());
        location.setMontantTotal(calculerMontantTotal(location));
        return locationRepository.save(location);
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = getLocationById(id);
        if (location.getStatut() == StatutLocation.EN_COURS) {
            if (location.getVoitures() != null) {
                for (Voiture v : location.getVoitures()) {
                    voitureService.updateStatut(v.getId(), StatutVoiture.DISPONIBLE);
                }
            }
            if (location.getChauffeurs() != null) {
                for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                    c.setDisponible(true);
                }
            }
        }
        locationRepository.delete(location);
    }

    public List<Location> getLocationsEnCours() {
        return locationRepository.findByStatut(StatutLocation.EN_COURS);
    }

    public List<Location> getLocationsEnRetard() {
        return locationRepository.findByStatut(StatutLocation.EN_COURS).stream()
                .filter(l -> l.getDateFin().isBefore(LocalDate.now()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Location> getLocationsEnAttente() {
        return locationRepository.findByStatut(StatutLocation.EN_ATTENTE);
    }

    public long countEnCours() {
        return locationRepository.countByStatut(StatutLocation.EN_COURS);
    }

    public long countTerminees() {
        return locationRepository.countByStatut(StatutLocation.TERMINEE);
    }

    public long countAnnulees() {
        return locationRepository.countByStatut(StatutLocation.ANNULEE);
    }

    public long countAll() {
        return locationRepository.count();
    }

    public List<Integer> getLocationsParMois() {
        List<Object[]> results = locationRepository.countLocationsParMois();
        List<Integer> locationsParMois = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) locationsParMois.add(0);

        for (Object[] result : results) {
            int mois = ((Number) result[0]).intValue() - 1;
            int count = ((Number) result[1]).intValue();
            locationsParMois.set(mois, count);
        }
        return locationsParMois;
    }

    public List<Location> getDernieresLocations(int limit) {
        return locationRepository.findTop5ByOrderByDateDebutDesc();
    }

    public Location updateStatut(Long id, StatutLocation statut) {
        Location location = getLocationById(id);
        StatutLocation ancienStatut = location.getStatut();
        location.setStatut(statut);

        if (statut == StatutLocation.TERMINEE || statut == StatutLocation.ANNULEE) {
            if (ancienStatut == StatutLocation.EN_COURS) {
                if (location.getVoitures() != null) {
                    for (Voiture v : location.getVoitures()) {
                        voitureService.updateStatut(v.getId(), StatutVoiture.DISPONIBLE);
                    }
                }
                if (location.getChauffeurs() != null) {
                    for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                        c.setDisponible(true);
                    }
                }
            }
        }
        return locationRepository.save(location);
    }

    public double calculerMontantTotal(Location location) {
        long nombreJours = ChronoUnit.DAYS.between(location.getDateDebut(), location.getDateFin());
        if (nombreJours <= 0) nombreJours = 1;

        double montantVoitures = 0;
        if (location.getVoitures() != null) {
            for (Voiture v : location.getVoitures()) {
                montantVoitures += nombreJours * (v.getPrixParJour() != null ? v.getPrixParJour() : 0);
            }
        }

        double montantChauffeurs = 0;
        if (location.getChauffeurs() != null) {
            for (org.example.location_voiture.model.Chauffeur c : location.getChauffeurs()) {
                montantChauffeurs += nombreJours * c.getTarifJournalier();
            }
        }
        return montantVoitures + montantChauffeurs;
    }

    public List<Location> getLocationsPayantes() {
        // Retourne les locations qui ont au moins un paiement EFFECTUE
        // En conditions réelles, on pourrait aussi vérifier si le montant est total
        return locationRepository.findLocationsWithSuccessfulPayments();
    }

    public List<Location> searchLocations(String search, String statutStr, LocalDate dateDebut, LocalDate dateFin) {
        StatutLocation statut = (statutStr != null && !statutStr.isEmpty()) ? StatutLocation.valueOf(statutStr) : null;
        
        return getAllLocations().stream()
                .filter(l -> search == null || search.isEmpty() || 
                             (l.getClient() != null && (l.getClient().getNom().toLowerCase().contains(search.toLowerCase()) || 
                                                       (l.getClient().getPrenom() != null && l.getClient().getPrenom().toLowerCase().contains(search.toLowerCase())))))
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> dateDebut == null || !l.getDateDebut().isBefore(dateDebut))
                .filter(l -> dateFin == null || !l.getDateFin().isAfter(dateFin))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Object[]> getPopularVoitures(int limit) {
        return locationRepository.findTopPopularVoitures(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    public List<Object[]> getTopSpendingClients(int limit) {
        return locationRepository.findTopSpendingClients(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    public long getPendingPaymentCountByClient(org.example.location_voiture.model.Client client) {
        return locationRepository.countPendingPaymentsByClient(client);
    }

    public List<Location> getPendingPaymentsByClient(org.example.location_voiture.model.Client client) {
        return locationRepository.findPendingPaymentsByClient(client);
    }
}