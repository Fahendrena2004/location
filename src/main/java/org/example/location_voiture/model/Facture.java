package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "factures")
@Data
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String numeroFacture;
    private java.time.LocalDate dateEmission;
    
    @Enumerated(EnumType.STRING)
    private org.example.location_voiture.model.enums.Currency devise = org.example.location_voiture.model.enums.Currency.MGA;
    
    @Enumerated(EnumType.STRING)
    private org.example.location_voiture.model.enums.StatutFacture statut = org.example.location_voiture.model.enums.StatutFacture.EN_ATTENTE;
    
    private Double tauxChangeApplique = 1.0;

    private Double montantHT;
    private Double tva;
    private Double penalite = 0.0;
    private Double montantTTC;
    
    @ManyToOne
    @JoinColumn(name = "compte_paiement_id")
    private ComptePaiement comptePaiement;


}