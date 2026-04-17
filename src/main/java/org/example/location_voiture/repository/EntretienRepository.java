package org.example.location_voiture.repository;

import org.example.location_voiture.model.Entretien;
import org.example.location_voiture.model.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {
    List<Entretien> findByVoiture(Voiture voiture);
    List<Entretien> findByTermineFalse();

    // Alertes : entretiens non terminés prévus dans les 7 prochains jours ou en retard
    @Query("SELECT e FROM Entretien e WHERE e.termine = false AND e.dateEntretien <= :alertDate ORDER BY e.dateEntretien ASC")
    List<Entretien> findUpcomingAlerts(@Param("alertDate") LocalDate alertDate);
}