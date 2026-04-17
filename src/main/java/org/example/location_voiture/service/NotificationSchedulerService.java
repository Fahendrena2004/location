package org.example.location_voiture.service;

import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.repository.LocationRepository;
import org.example.location_voiture.repository.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationSchedulerService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private EmailService emailService;

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);

        // 1. Rappel fin de location
        List<Location> locationsEndingTomorrow = locationRepository.findAll().stream()
                .filter(l -> tomorrow.equals(l.getDateFin()) && l.getStatut() != null && l.getStatut().name().equals("EN_COURS"))
                .toList();

        for (Location location : locationsEndingTomorrow) {
            if (location.getClient() != null && location.getClient().getEmail() != null) {
                String subject = "Rappel : Fin de votre location demain";
                String body = "Bonjour " + location.getClient().getPrenom() + ",\n\n" +
                        "Ceci est un rappel que votre location de voiture (Réf: #" + location.getId() + ") se termine demain (" + tomorrow + ").\n" +
                        "Merci de restituer le véhicule à temps.\n\nCordialement,\nL'équipe Location Voiture";
                emailService.sendTextEmail(location.getClient().getEmail(), subject, body);
            }
        }

        // 2. Rappel pour l'Admin (Assurance / Visite Technique)
        List<Voiture> voitures = voitureRepository.findAll();
        for (Voiture voiture : voitures) {
            String adminEmail = "admin@locationvoiture.com"; // Email par défaut de l'administrateur
            
            if (nextWeek.equals(voiture.getDateExpirationAssurance())) {
                String subject = "Alerte : Expiration de l'assurance";
                String body = "L'assurance de la voiture " + voiture.getMarque() + " " + voiture.getModele() + " (" + voiture.getPlaqueImmatriculation() + ") expire dans 7 jours (" + nextWeek + ").";
                emailService.sendTextEmail(adminEmail, subject, body);
            }

            if (nextWeek.equals(voiture.getDateProchaineVisiteTechnique())) {
                String subject = "Alerte : Visite Technique à prévoir";
                String body = "La visite technique de la voiture " + voiture.getMarque() + " " + voiture.getModele() + " (" + voiture.getPlaqueImmatriculation() + ") est prévue dans 7 jours (" + nextWeek + ").";
                emailService.sendTextEmail(adminEmail, subject, body);
            }
        }
    }
}
