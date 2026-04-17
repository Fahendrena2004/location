package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.example.location_voiture.model.enums.StatutPaiement;

@Entity
@Table(name = "paiements")
@Data
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Positive(message = "Le montant doit être positif")
    @NotNull(message = "Le montant est obligatoire")
    private Double montant;

    @NotNull(message = "La date de paiement est obligatoire")
    private LocalDate datePaiement;

    @NotBlank(message = "Le mode de paiement est obligatoire")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_-]+$", message = "Le mode de paiement contient des caractères non autorisés")
    private String modePaiement;

    @Pattern(regexp = "^[a-zA-Z0-9\\s_-]+$", message = "La référence contient des caractères non autorisés")
    private String reference;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut = StatutPaiement.EFFECTUE;
}