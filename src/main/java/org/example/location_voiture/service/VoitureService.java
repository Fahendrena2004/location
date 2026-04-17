package org.example.location_voiture.service;

import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.model.enums.StatutVoiture;
import org.example.location_voiture.repository.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VoitureService {

    @Autowired
    private VoitureRepository voitureRepository;

    public List<Voiture> getAllVoitures() {
        return voitureRepository.findAll();
    }

    public Voiture getVoitureById(Long id) {
        return voitureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voiture non trouvée avec l'id: " + id));
    }

    public Voiture saveVoiture(Voiture voiture) {
        voiture.setStatut(StatutVoiture.DISPONIBLE);
        return voitureRepository.save(voiture);
    }

    public Voiture updateVoiture(Long id, Voiture voitureDetails) {
        Voiture voiture = getVoitureById(id);
        voiture.setMarque(voitureDetails.getMarque());
        voiture.setModele(voitureDetails.getModele());
        voiture.setAnnee(voitureDetails.getAnnee());
        voiture.setPlaqueImmatriculation(voitureDetails.getPlaqueImmatriculation());
        voiture.setPrixParJour(voitureDetails.getPrixParJour());
        voiture.setDescription(voitureDetails.getDescription());
        voiture.setImages(voitureDetails.getImages());
        return voitureRepository.save(voiture);
    }

    public void deleteVoiture(Long id) {
        Voiture voiture = getVoitureById(id);
        voitureRepository.delete(voiture);
    }

    public List<Voiture> getVoituresDisponibles() {
        return voitureRepository.findByStatut(StatutVoiture.DISPONIBLE);
    }

    public long countVoitures() {
        return voitureRepository.count();
    }

    public long countDisponibles() {
        return voitureRepository.countByStatut(StatutVoiture.DISPONIBLE);
    }

    public long countLouees() {
        return voitureRepository.countByStatut(StatutVoiture.LOUE);
    }

    public Voiture updateStatut(Long id, StatutVoiture statut) {
        Voiture voiture = getVoitureById(id);
        voiture.setStatut(statut);
        return voitureRepository.save(voiture);
    }

    public Page<Voiture> searchVoitures(String search, String categorie, String transmission, String carburant, Double prixMax, Pageable pageable) {
        Specification<Voiture> spec = Specification.where(null);
        
        if (search != null && !search.trim().isEmpty()) {
            final String searchLower = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("marque")), searchLower),
                cb.like(cb.lower(root.get("modele")), searchLower),
                cb.like(cb.lower(root.get("plaqueImmatriculation")), searchLower)
            ));
        }
        
        if (categorie != null && !categorie.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("categorie")), categorie.toLowerCase()));
        }
        
        if (transmission != null && !transmission.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("transmission")), transmission.toLowerCase()));
        }
        
        if (carburant != null && !carburant.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("carburant")), carburant.toLowerCase()));
        }
        
        if (prixMax != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("prixParJour"), prixMax));
        }

        return voitureRepository.findAll(spec, pageable);
    }
}