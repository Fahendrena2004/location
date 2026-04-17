package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.example.location_voiture.model.enums.StatutVoiture;

@Entity
@Table(name = "voitures")
@Data
public class Voiture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La marque est obligatoire")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\'-]+$", message = "La marque contient des caractères non autorisés")
    @Column(nullable = false)
    private String marque;

    @NotBlank(message = "Le modèle est obligatoire")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\'-]+$", message = "Le modèle contient des caractères non autorisés")
    @Column(nullable = false)
    private String modele;

    @Min(value = 1900, message = "Année invalide")
    @Max(value = 2100, message = "Année invalide")
    private Integer annee;

    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Pattern(regexp = "^[a-zA-Z0-9\\s-]+$", message = "La plaque contient des caractères non autorisés")
    @Column(unique = true)
    private String plaqueImmatriculation;

    @Positive(message = "Le prix doit être positif")
    @NotNull(message = "Le prix est obligatoire")
    private Double prixParJour;

    @Column(length = 50)
    private String categorie; // "Berline", "SUV", "Citadine", "Luxe", "Utilitaire"

    private Integer places;

    @Column(length = 50)
    private String transmission; // "Automatique", "Manuelle"

    @Column(length = 50)
    private String carburant; // "Essence", "Diesel", "Electrique", "Hybride"

    @Column(length = 50)
    private String couleur;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "voiture_images", joinColumns = @JoinColumn(name = "voiture_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Enumerated(EnumType.STRING)
    private StatutVoiture statut = StatutVoiture.DISPONIBLE;

    @ManyToMany(mappedBy = "voitures")
    private List<Location> locations;

    @OneToMany(mappedBy = "voiture")
    private List<Entretien> entretiens;

    private Integer kilometrage = 0;

    private LocalDate dateExpirationAssurance;
    private LocalDate dateProchaineVisiteTechnique;
}