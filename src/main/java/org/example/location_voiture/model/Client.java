package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String typeClient = "PERSONNE"; // PERSONNE ou ASSOCIATION

    @NotBlank(message = "Le nom est obligatoire")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\'-]+$", message = "Le nom contient des caractères non autorisés")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\'-]+$", message = "Le prénom contient des caractères non autorisés")
    @Column(nullable = false)
    private String prenom;

    @Email(message = "Format d'email invalide")
    @Column(unique = true)
    private String email;

    private LocalDate dateNaissance;

    @Pattern(regexp = "^[0-9a-zA-Z\\s-]+$", message = "Le CIN contient des caractères non autorisés")
    private String cin;

    @Pattern(regexp = "^[0-9\\s\\+.-]+$", message = "Le numéro de téléphone contient des caractères non autorisés")
    private String telephone;

    private String adresse;

    @Pattern(regexp = "^[0-9a-zA-Z\\s-]+$", message = "Le numéro de permis contient des caractères non autorisés")
    private String numeroPermis;

    @Column(name = "avec_chauffeur", nullable = false)
    private boolean avecChauffeur = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "utilisateur_id", referencedColumnName = "id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User utilisateur;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations;

    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

}