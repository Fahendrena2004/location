package org.example.location_voiture.repository;

import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.enums.StatutLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByStatut(StatutLocation statut);
    long countByStatut(StatutLocation statut);
    List<Location> findByStatutIn(List<StatutLocation> statuts);
    List<Location> findByClient(org.example.location_voiture.model.Client client);
    @Query("SELECT COUNT(l) FROM Location l WHERE l.client = :client " +
           "AND l.statut IN (org.example.location_voiture.model.enums.StatutLocation.EN_COURS, org.example.location_voiture.model.enums.StatutLocation.TERMINEE) " +
           "AND NOT EXISTS (SELECT p FROM Paiement p WHERE p.location = l AND p.statut = org.example.location_voiture.model.enums.StatutPaiement.EFFECTUE)")
    long countPendingPaymentsByClient(@Param("client") org.example.location_voiture.model.Client client);

    @Query("SELECT l FROM Location l WHERE l.client = :client " +
           "AND l.statut IN (org.example.location_voiture.model.enums.StatutLocation.EN_COURS, org.example.location_voiture.model.enums.StatutLocation.TERMINEE) " +
           "AND NOT EXISTS (SELECT p FROM Paiement p WHERE p.location = l AND p.statut = org.example.location_voiture.model.enums.StatutPaiement.EFFECTUE)")
    List<Location> findPendingPaymentsByClient(@Param("client") org.example.location_voiture.model.Client client);

    @Query("SELECT EXTRACT(MONTH FROM l.dateDebut) as mois, COUNT(l) as total FROM Location l GROUP BY EXTRACT(MONTH FROM l.dateDebut)")
    List<Object[]> countLocationsParMois();

    List<Location> findTop5ByOrderByDateDebutDesc();

    // Vérifie si une voiture est déjà réservée sur une période donnée (chevauchement)
    @Query("SELECT COUNT(l) > 0 FROM Location l JOIN l.voitures v " +
           "WHERE v.id = :voitureId " +
           "AND l.statut IN ('EN_ATTENTE', 'EN_COURS') " +
           "AND l.id <> :excludeId " +
           "AND l.dateDebut < :dateFin AND l.dateFin > :dateDebut")
    boolean existsOverlap(@Param("voitureId") Long voitureId,
                          @Param("dateDebut") LocalDate dateDebut,
                          @Param("dateFin") LocalDate dateFin,
                          @Param("excludeId") Long excludeId);

    @Query("SELECT DISTINCT l FROM Location l JOIN Paiement p ON p.location = l WHERE p.statut = org.example.location_voiture.model.enums.StatutPaiement.EFFECTUE")
    List<Location> findLocationsWithSuccessfulPayments();

    @Query("SELECT DISTINCT l FROM Location l " +
           "LEFT JOIN l.client c " +
           "LEFT JOIN l.voitures v " +
           "WHERE (:query IS NULL OR " +
           " LOWER(c.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.prenom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.plaqueImmatriculation) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(v.marque) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:statut IS NULL OR l.statut = :statut) " +
           "ORDER BY l.dateDebut DESC")
    List<Location> search(@Param("query") String query, @Param("statut") StatutLocation statut);

    @Query("SELECT v, COUNT(l) as count " +
           "FROM Location l JOIN l.voitures v " +
           "GROUP BY v " +
           "ORDER BY count DESC")
    List<Object[]> findTopPopularVoitures(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c, SUM(l.montantTotal) as total FROM Location l JOIN l.client c GROUP BY c ORDER BY total DESC")
    List<Object[]> findTopSpendingClients(org.springframework.data.domain.Pageable pageable);
}