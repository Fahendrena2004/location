package org.example.location_voiture.controller;

import org.example.location_voiture.model.User;
import org.example.location_voiture.service.NotificationService;
import org.example.location_voiture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listNotifications(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("notifications", notificationService.getRecentNotifications(user));
        model.addAttribute("page", "notifications");
        return "notifications/liste";
    }

    @PostMapping("/tout-lire")
    public String markAllAsRead(Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }

    @GetMapping("/lire/{id}")
    public String readNotification(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam(required = false) String redirect) {
        notificationService.markAsRead(id);
        if (redirect != null && !redirect.isEmpty()) {
            return "redirect:" + redirect;
        }
        return "redirect:/notifications";
    }
}
