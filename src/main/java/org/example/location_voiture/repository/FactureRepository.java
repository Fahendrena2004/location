package org.example.location_voiture.repository;

import org.example.location_voiture.model.Facture;
import org.example.location_voiture.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    Facture findByLocation(Location location);
    Facture findByNumeroFacture(String numeroFacture);
    java.util.List<Facture> findByLocationClient(org.example.location_voiture.model.Client client);
}