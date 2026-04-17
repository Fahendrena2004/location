package org.example.location_voiture.config;

import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.repository.VoitureRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ImageSeeder implements CommandLineRunner {

    private final VoitureRepository voitureRepository;

    public ImageSeeder(VoitureRepository voitureRepository) {
        this.voitureRepository = voitureRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        List<Voiture> voitures = voitureRepository.findAll();
        for (Voiture voiture : voitures) {
            // Uniquement si la voiture n'a pas encore d'images (cas d'une nouvelle base ou voiture non migrée)
            if (voiture.getImages() == null || voiture.getImages().isEmpty()) {
                String marque = voiture.getMarque() != null ? voiture.getMarque().toLowerCase() : "";
                
                if (marque.contains("toyota")) {
                    voiture.setImages(Arrays.asList("/img/voitures/corolla.png"));
                    if (voiture.getCategorie() == null) {
                        voiture.setCategorie("Berline"); voiture.setCouleur("Blanc Perle"); voiture.setPlaces(5); voiture.setCarburant("Hybride"); voiture.setTransmission("Automatique");
                    }
                } else if (marque.contains("peugeot")) {
                    voiture.setImages(Arrays.asList("/img/voitures/peugeot208.png"));
                    if (voiture.getCategorie() == null) {
                        voiture.setCategorie("Citadine"); voiture.setCouleur("Jaune Faro"); voiture.setPlaces(5); voiture.setCarburant("Essence"); voiture.setTransmission("Manuelle");
                    }
                } else if (marque.contains("bmw")) {
                    voiture.setImages(Arrays.asList("/img/voitures/bmw3.png"));
                    if (voiture.getCategorie() == null) {
                        voiture.setCategorie("Berline"); voiture.setCouleur("Noir Minéral"); voiture.setPlaces(5); voiture.setCarburant("Diesel"); voiture.setTransmission("Automatique");
                    }
                } else if (marque.contains("mercedes")) {
                    voiture.setImages(Arrays.asList("/img/voitures/mercedes.png"));
                    if (voiture.getCategorie() == null) {
                        voiture.setCategorie("Luxe"); voiture.setCouleur("Gris Sélénite"); voiture.setPlaces(5); voiture.setCarburant("Hybride"); voiture.setTransmission("Automatique");
                    }
                }
                voitureRepository.save(voiture);
            }
        }
    }
}
