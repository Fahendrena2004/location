package org.example.location_voiture.repository;

import org.example.location_voiture.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByVoitureIdOrderByCreatedAtDesc(Long voitureId);
    double countByVoitureId(Long voitureId);
}
