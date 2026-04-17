package org.example.location_voiture.service;

import org.example.location_voiture.model.Entretien;
import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.repository.EntretienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EntretienService {

    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private VoitureService voitureService;

    public List<Entretien> getAllEntretiens() {
        return entretienRepository.findAll();
    }

    public Entretien getEntretienById(Long id) {
        return entretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé avec l'id: " + id));
    }

    @org.springframework.transaction.annotation.Transactional
    public Entretien saveEntretien(Entretien entretien) {
        if (!entretien.getTermine()) {
            voitureService.updateStatut(entretien.getVoiture().getId(), org.example.location_voiture.model.enums.StatutVoiture.MAINTENANCE);
        }
        return entretienRepository.save(entretien);
    }

    @org.springframework.transaction.annotation.Transactional
    public Entretien updateEntretien(Long id, Entretien entretienDetails) {
        Entretien entretien = getEntretienById(id);
        entretien.setDateEntretien(entretienDetails.getDateEntretien());
        entretien.setTypeEntretien(entretienDetails.getTypeEntretien());
        entretien.setDescription(entretienDetails.getDescription());
        entretien.setCout(entretienDetails.getCout());
        entretien.setMecanicien(entretienDetails.getMecanicien());
        entretien.setKilometrage(entretienDetails.getKilometrage());
        
        boolean wasTermine = entretien.getTermine();
        entretien.setTermine(entretienDetails.getTermine());
        
        if (!wasTermine && entretien.getTermine()) {
            if (entretien.getKilometrage() != null) updateVoitureMileage(entretien);
            checkAndRestoreVoitureStatus(entretien.getVoiture());
        } else if (wasTermine && !entretien.getTermine()) {
            voitureService.updateStatut(entretien.getVoiture().getId(), org.example.location_voiture.model.enums.StatutVoiture.MAINTENANCE);
        }
        
        return entretienRepository.save(entretien);
    }

    private void updateVoitureMileage(Entretien e) {
        Voiture v = e.getVoiture();
        if (v != null && (v.getKilometrage() == null || (e.getKilometrage() != null && e.getKilometrage() > v.getKilometrage()))) {
            v.setKilometrage(e.getKilometrage());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteEntretien(Long id) {
        Entretien entretien = getEntretienById(id);
        Voiture voiture = entretien.getVoiture();
        entretienRepository.delete(entretien);
        if (!entretien.getTermine()) {
            checkAndRestoreVoitureStatus(voiture);
        }
    }

    private void checkAndRestoreVoitureStatus(Voiture voiture) {
        List<Entretien> activeEntretiens = entretienRepository.findByVoiture(voiture).stream()
                .filter(e -> !e.getTermine())
                .collect(java.util.stream.Collectors.toList());
        
        if (activeEntretiens.isEmpty()) {
            voitureService.updateStatut(voiture.getId(), org.example.location_voiture.model.enums.StatutVoiture.DISPONIBLE);
        }
    }

    public List<Entretien> getEntretiensEnCours() {
        return entretienRepository.findByTermineFalse();
    }

    public List<Entretien> getEntretiensByVoiture(Long voitureId) {
        Voiture voiture = voitureService.getVoitureById(voitureId);
        return entretienRepository.findByVoiture(voiture);
    }

    @org.springframework.transaction.annotation.Transactional
    public Entretien toggleStatus(Long id) {
        Entretien entretien = getEntretienById(id);
        boolean newStatus = !entretien.getTermine();
        entretien.setTermine(newStatus);
        
        if (newStatus) {
            if (entretien.getKilometrage() != null) updateVoitureMileage(entretien);
            checkAndRestoreVoitureStatus(entretien.getVoiture());
        } else {
            voitureService.updateStatut(entretien.getVoiture().getId(), org.example.location_voiture.model.enums.StatutVoiture.MAINTENANCE);
        }
        
        return entretienRepository.save(entretien);
    }
}