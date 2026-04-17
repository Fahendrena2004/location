package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "comptes_paiements")
@Data
public class ComptePaiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomBanque; // Exemple: BNC, BOA, MVola, Orange Money

    @Column(nullable = false)
    private String nomTitulaire;

    @Column(nullable = false)
    private String numeroCompte;

    private String iban;

    private boolean actif = true;
}
