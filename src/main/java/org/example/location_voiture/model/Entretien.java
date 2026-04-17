package org.example.location_voiture.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "entretiens")
@Data
public class Entretien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voiture_id")
    private Voiture voiture;

    @NotNull(message = "La date d'entretien est obligatoire")
    private LocalDate dateEntretien;

    @NotBlank(message = "Le type d'entretien est obligatoire")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\'-]+$", message = "Le type contient des caractères non autorisés")
    private String typeEntretien;

    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\',.?!-]*$", message = "La description contient des caractères non autorisés")
    private String description;

    @PositiveOrZero(message = "Le coût doit être positif")
    private Double cout;

    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\'-]+$", message = "Le nom du mécanicien contient des caractères non autorisés")
    private String mecanicien;

    @PositiveOrZero(message = "Le kilométrage doit être positif")
    private Integer kilometrage;
    private Boolean termine = false;
}