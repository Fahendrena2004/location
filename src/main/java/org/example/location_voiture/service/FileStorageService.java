package org.example.location_voiture.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.coobird.thumbnailator.Thumbnails;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire où les images uploadées seront stockées.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.jpg");

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Echec de sécurité, nom de fichier invalide : " + fileName);
            }

            // Validation de l'extension
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1).toLowerCase();
            }

            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
            if (!allowedExtensions.contains(extension)) {
                throw new RuntimeException("Format de fichier non autorisé. Seuls les fichiers JPG, PNG et WEBP sont acceptés.");
            }

            // Générer un nom de fichier unique avec UUID pour éviter les conflits et écrasements sur le serveur
            String uniqueFileName = UUID.randomUUID().toString() + ".jpg"; // On convertit tout en jpg pour standardiser

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            
            // On utilise Thumbnailator pour redimensionner (max 1200x800) et optimiser
            Thumbnails.of(file.getInputStream())
                    .size(1200, 800)
                    .outputFormat("jpg")
                    .outputQuality(0.85) // Compression légère pour le web
                    .toFile(targetLocation.toFile());

            return "/uploads/" + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de stocker et redimensionner l'image " + file.getOriginalFilename() + ". Veuillez réessayer!", ex);
        }
    }
}
