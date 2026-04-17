package org.example.location_voiture.controller.api;

import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.model.enums.StatutVoiture;
import org.example.location_voiture.repository.LocationRepository;
import org.example.location_voiture.repository.VoitureRepository;
import org.example.location_voiture.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationRestController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private LocationRepository locationRepository;

    @GetMapping("/all-events")
    public List<Map<String, Object>> getEvents() {
        return locationService.getAllLocations().stream().map(loc -> {
            Map<String, Object> event = new HashMap<>();
            String vehicles = loc.getVoitures().stream()
                    .map(v -> v.getMarque() + " " + v.getModele())
                    .collect(Collectors.joining(", "));

            String clientName = (loc.getClient() != null)
                    ? (loc.getClient().getNom() + " " + (loc.getClient().getPrenom() != null ? loc.getClient().getPrenom() : ""))
                    : "Client Inconnu";

            event.put("id", loc.getId());
            event.put("title", clientName + " (" + vehicles + ")");
            event.put("start", loc.getDateDebut().toString());
            event.put("end", loc.getDateFin().plusDays(1).toString());
            event.put("allDay", true);

            if (loc.getStatut() != null) {
                switch (loc.getStatut()) {
                    case EN_ATTENTE:
                        event.put("backgroundColor", "rgba(245, 158, 11, 0.2)");
                        event.put("borderColor", "#f59e0b");
                        event.put("textColor", "#f59e0b");
                        break;
                    case PAYEE:
                        event.put("backgroundColor", "rgba(52, 211, 153, 0.2)");
                        event.put("borderColor", "#34d399");
                        event.put("textColor", "#34d399");
                        break;
                    case EN_COURS:
                        event.put("backgroundColor", "rgba(37, 99, 235, 0.2)");
                        event.put("borderColor", "#2563eb");
                        event.put("textColor", "#2563eb");
                        break;
                    case TERMINEE:
                        event.put("backgroundColor", "rgba(16, 185, 129, 0.2)");
                        event.put("borderColor", "#10b981");
                        event.put("textColor", "#10b981");
                        break;
                    case ANNULEE:
                        event.put("backgroundColor", "rgba(239, 68, 68, 0.2)");
                        event.put("borderColor", "#ef4444");
                        event.put("textColor", "#ef4444");
                        break;
                    default:
                        break;
                }
            }

            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("statut", loc.getStatut());
            extendedProps.put("montant", loc.getMontantTotal());
            extendedProps.put("locationId", loc.getId());
            event.put("extendedProps", extendedProps);

            return event;
        }).collect(Collectors.toList());
    }

    /**
     * Returns list of available voitures (DISPONIBLE status + no overlapping reservation)
     * for a given date range. Used by the reservation form to dynamically filter cars.
     */
    @GetMapping("/voitures/disponibles")
    public List<Map<String, Object>> getVoituresDisponiblesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        List<Voiture> candidats = voitureRepository.findByStatut(StatutVoiture.DISPONIBLE);

        return candidats.stream()
            .filter(v -> !locationRepository.existsOverlap(v.getId(), dateDebut, dateFin, -1L))
            .map(v -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", v.getId());
                map.put("marque", v.getMarque());
                map.put("modele", v.getModele());
                map.put("prixParJour", v.getPrixParJour());
                map.put("categorie", v.getCategorie() != null ? v.getCategorie() : "");
                map.put("transmission", v.getTransmission() != null ? v.getTransmission() : "");
                map.put("carburant", v.getCarburant() != null ? v.getCarburant() : "");
                map.put("image", (v.getImages() != null && !v.getImages().isEmpty()) ? v.getImages().get(0) : null);
                return map;
            }).collect(Collectors.toList());
    }
}
