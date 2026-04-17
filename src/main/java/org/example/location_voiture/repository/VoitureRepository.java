package org.example.location_voiture.repository;

import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.model.enums.StatutVoiture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface VoitureRepository extends JpaRepository<Voiture, Long>, JpaSpecificationExecutor<Voiture> {
    List<Voiture> findByStatut(StatutVoiture statut);
    long countByStatut(StatutVoiture statut);
}