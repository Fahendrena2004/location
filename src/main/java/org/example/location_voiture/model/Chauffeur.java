package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "chauffeurs")
@Data
public class Chauffeur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\'-]+$", message = "Le nom contient des caractères non autorisés")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\'-]+$", message = "Le prénom contient des caractères non autorisés")
    @Column(nullable = false)
    private String prenom;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^[0-9\\s\\+.-]+$", message = "Le numéro de téléphone contient des caractères non autorisés")
    @Column(unique = true)
    private String telephone;

    @Pattern(regexp = "^[0-9a-zA-Z\\s-]+$", message = "Le numéro de permis contient des caractères non autorisés")
    private String numeroPermis;

    @Positive(message = "Le tarif doit être positif")
    private double tarifJournalier;

    private boolean disponible = true;

    @ManyToMany(mappedBy = "chauffeurs")
    private List<Location> locations;
}
