package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import org.example.location_voiture.model.enums.StatutLocation;

@Entity
@Table(name = "locations")
@Data
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
        name = "location_voitures",
        joinColumns = @JoinColumn(name = "location_id"),
        inverseJoinColumns = @JoinColumn(name = "voiture_id")
    )
    private List<Voiture> voitures;

    @ManyToMany
    @JoinTable(
        name = "location_chauffeurs",
        joinColumns = @JoinColumn(name = "location_id"),
        inverseJoinColumns = @JoinColumn(name = "chauffeur_id")
    )
    private List<Chauffeur> chauffeurs;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDate dateRetourEffective;
    private Double montantTotal;

    @Enumerated(EnumType.STRING)
    private StatutLocation statut = StatutLocation.EN_COURS;

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private Facture facture;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paiement> paiements;

    @Column(columnDefinition = "TEXT")
    private String signatureClient;

    @Column(name = "paiement_declare")
    private Boolean paiementDeclare = false;

    @Column(name = "mode_paiement_choisi")
    private String modePaiementChoisi;

    public Boolean getPaiementDeclare() {
        return paiementDeclare != null ? paiementDeclare : false;
    }

    public boolean isPayee() {
        return statut == StatutLocation.PAYEE || 
               (paiements != null && paiements.stream().anyMatch(p -> p.getStatut() == org.example.location_voiture.model.enums.StatutPaiement.EFFECTUE));
    }
}