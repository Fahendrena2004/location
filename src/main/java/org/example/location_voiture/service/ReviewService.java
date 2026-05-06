package org.example.location_voiture.service;

import org.example.location_voiture.model.Review;
import org.example.location_voiture.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsByVoiture(Long voitureId) {
        return reviewRepository.findByVoitureIdOrderByCreatedAtDesc(voitureId);
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public double getAverageRating(Long voitureId) {
        List<Review> reviews = reviewRepository.findByVoitureIdOrderByCreatedAtDesc(voitureId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
}
