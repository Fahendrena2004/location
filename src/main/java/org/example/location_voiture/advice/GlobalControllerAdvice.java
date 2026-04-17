package org.example.location_voiture.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;
import java.util.List;
import java.util.Collections;
import org.example.location_voiture.service.UserService;
import org.example.location_voiture.service.NotificationService;
import org.example.location_voiture.model.User;
import org.example.location_voiture.model.Notification;
import org.example.location_voiture.service.LocationService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LocationService locationService;

    @ModelAttribute("unreadNotificationsCount")
    public long getUnreadNotificationsCount(Principal principal) {
        if (principal == null) return 0;
        try {
            User user = userService.getUserByEmail(principal.getName());
            return notificationService.getUnreadCount(user);
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("recentNotifications")
    public List<Notification> getRecentNotifications(Principal principal) {
        if (principal == null) return Collections.emptyList();
        try {
            User user = userService.getUserByEmail(principal.getName());
            return notificationService.getRecentNotifications(user);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @ModelAttribute("pendingPaymentsCount")
    public long getPendingPaymentsCount(Principal principal) {
        if (principal == null) return 0;
        try {
            User user = userService.getUserByEmail(principal.getName());
            if (user.getClient() != null) {
                return locationService.getPendingPaymentCountByClient(user.getClient());
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("requestURI")
    public String requestURI(final HttpServletRequest request) {
        if (request != null) {
            return request.getRequestURI();
        }
        return "";
    }
}
