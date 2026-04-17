package org.example.location_voiture.service;

import org.example.location_voiture.model.Notification;
import org.example.location_voiture.model.User;
import org.example.location_voiture.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(String titre, String description, String url, User user) {
        Notification notif = Notification.builder()
                .titre(titre)
                .description(description)
                .url(url)
                .user(user)
                .build();
        notificationRepository.save(notif);
    }

    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop10ByUserOrderByDateCreationDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndLuFalse(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findAllByUser(user);
        for (Notification n : unread) {
            n.setLu(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setLu(true);
            notificationRepository.save(n);
        });
    }
}
