package org.example.location_voiture.controller;

import org.example.location_voiture.model.Location;
import org.example.location_voiture.model.User;
import org.example.location_voiture.model.enums.Role;
import org.example.location_voiture.service.LocationService;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
public class ApiLocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService userService;

    @GetMapping("/events")
    public List<Map<String, Object>> getCalendarEvents(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        List<Location> locations;

        if (user.getRole() == Role.ADMIN) {
            locations = locationService.getAllLocations();
        } else if (user.getClient() != null) {
            locations = locationService.getLocationsByClient(user.getClient());
        } else {
            locations = new ArrayList<>();
        }

        List<Map<String, Object>> events = new ArrayList<>();

        for (Location loc : locations) {
            if (loc.getDateDebut() == null || loc.getDateFin() == null) continue;

            Map<String, Object> event = new HashMap<>();
            
            // Format title
            String carDesc = "";
            if (loc.getVoitures() != null && !loc.getVoitures().isEmpty()) {
                carDesc = loc.getVoitures().get(0).getMarque() + " " + loc.getVoitures().get(0).getModele();
            }
            String title = loc.getClient() != null ? loc.getClient().getNom() + " - " + carDesc : carDesc;

            event.put("id", loc.getId());
            event.put("title", title);
            event.put("start", loc.getDateDebut().toString());
            // Add 1 day to end date because FullCalendar end dates are exclusive
            event.put("end", loc.getDateFin().plusDays(1).toString());
            event.put("url", "/locations/" + loc.getId());

            // Color based on status
            String color = "#3b82f6"; // default primary
            if (loc.getStatut() != null) {
                switch (loc.getStatut()) {
                    case EN_ATTENTE: color = "#f59e0b"; break; // warning
                    case PAYEE: color = "#8b5cf6"; break; // purple
                    case EN_COURS: color = "#2563eb"; break; // primary
                    case TERMINEE: color = "#10b981"; break; // success
                    case ANNULEE: color = "#ef4444"; break; // danger
                    default: break;
                }
            }
            
            event.put("backgroundColor", color);
            event.put("borderColor", color);
            // Convert custom colors to slightly transparent background for event look
            event.put("className", "shadow-sm border-0");

            events.add(event);
        }

        return events;
    }
}
