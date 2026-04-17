package org.example.location_voiture.service;

import org.example.location_voiture.model.ComptePaiement;
import org.example.location_voiture.repository.ComptePaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComptePaiementService {

    @Autowired
    private ComptePaiementRepository comptePaiementRepository;

    public List<ComptePaiement> getAllComptes() {
        return comptePaiementRepository.findAll();
    }

    public List<ComptePaiement> getComptesActifs() {
        return comptePaiementRepository.findByActifTrue();
    }

    public ComptePaiement getCompteById(Long id) {
        return comptePaiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte de paiement non trouvé avec l'id: " + id));
    }

    public ComptePaiement saveCompte(ComptePaiement compte) {
        return comptePaiementRepository.save(compte);
    }

    public void deleteCompte(Long id) {
        comptePaiementRepository.deleteById(id);
    }
}
