package org.example.location_voiture.repository;

import org.example.location_voiture.model.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChauffeurRepository extends JpaRepository<Chauffeur, Long> {
    List<Chauffeur> findByDisponibleTrue();
}
