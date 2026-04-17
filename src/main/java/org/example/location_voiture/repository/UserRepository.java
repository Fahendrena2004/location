package org.example.location_voiture.repository;

import org.example.location_voiture.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<User> findByRole(org.example.location_voiture.model.enums.Role role);
}
