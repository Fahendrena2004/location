package org.example.location_voiture.repository;

import org.example.location_voiture.model.ComptePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComptePaiementRepository extends JpaRepository<ComptePaiement, Long> {
    List<ComptePaiement> findByActifTrue();
}
