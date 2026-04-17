package org.example.location_voiture.repository;

import org.example.location_voiture.model.Paiement;
import org.example.location_voiture.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByLocation(Location location);
    List<Paiement> findByLocationClient(org.example.location_voiture.model.Client client);

    @Query("SELECT SUM(p.montant) FROM Paiement p")
    Double sumAllMontants();

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.datePaiement >= :startDate AND p.datePaiement <= :endDate")
    Double sumMontantsBetween(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE EXTRACT(MONTH FROM p.datePaiement) = EXTRACT(MONTH FROM CURRENT_DATE)")
    Double sumMontantsMoisCourant();
    
    @Query("SELECT EXTRACT(MONTH FROM p.datePaiement) as mois, SUM(p.montant) as total FROM Paiement p WHERE EXTRACT(YEAR FROM p.datePaiement) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY EXTRACT(MONTH FROM p.datePaiement)")
    List<Object[]> sumRevenusParMois();
    @Query("SELECT c.typeClient, SUM(p.montant) FROM Paiement p JOIN p.location l JOIN l.client c GROUP BY c.typeClient")
    List<Object[]> sumRevenusParTypeClient();
}