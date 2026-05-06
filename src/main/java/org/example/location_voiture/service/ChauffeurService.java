package org.example.location_voiture.service;

import org.example.location_voiture.model.Chauffeur;
import org.example.location_voiture.repository.ChauffeurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class ChauffeurService {

    @Autowired
    private ChauffeurRepository chauffeurRepository;

    public List<Chauffeur> getAllChauffeurs() {
        return chauffeurRepository.findAll();
    }

    public List<Chauffeur> getChauffeursDisponibles() {
        return chauffeurRepository.findByDisponibleTrue();
    }

    public Chauffeur getChauffeurById(Long id) {
        return chauffeurRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Chauffeur non trouvé avec l'id: " + id));
    }

    public Chauffeur saveChauffeur(Chauffeur chauffeur) {
        return chauffeurRepository.save(Objects.requireNonNull(chauffeur));
    }

    public void deleteChauffeur(Long id) {
        chauffeurRepository.deleteById(Objects.requireNonNull(id));
    }

    public Chauffeur updateDisponibilite(Long id, boolean disponible) {
        Chauffeur chauffeur = getChauffeurById(id);
        chauffeur.setDisponible(disponible);
        return chauffeurRepository.save(Objects.requireNonNull(chauffeur));
    }
}
