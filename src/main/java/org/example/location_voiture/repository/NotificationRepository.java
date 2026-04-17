package org.example.location_voiture.repository;

import org.example.location_voiture.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.example.location_voiture.model.User;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop10ByUserOrderByDateCreationDesc(User user);
    long countByUserAndLuFalse(User user);
    List<Notification> findAllByUser(User user);
}
