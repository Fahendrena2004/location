package org.example.location_voiture.controller;

import org.example.location_voiture.model.Review;
import org.example.location_voiture.model.User;
import org.example.location_voiture.model.Voiture;
import org.example.location_voiture.service.ReviewService;
import org.example.location_voiture.service.UserService;
import org.example.location_voiture.service.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private VoitureService voitureService;

    @PostMapping("/ajouter")
    @PreAuthorize("isAuthenticated()")
    public String addReview(@RequestParam("voitureId") Long voitureId,
                             @RequestParam("rating") int rating,
                             @RequestParam("comment") String comment,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        
        User user = userService.getUserByEmail(principal.getName());
        Voiture voiture = voitureService.getVoitureById(voitureId);

        if (voiture != null) {
            Review review = new Review();
            review.setRating(rating);
            review.setComment(comment);
            review.setUtilisateur(user);
            review.setVoiture(voiture);
            
            reviewService.saveReview(review);
            redirectAttributes.addFlashAttribute("message", "Merci pour votre avis !");
        } else {
            redirectAttributes.addFlashAttribute("error", "Voiture introuvable.");
        }

        return "redirect:/voitures/" + voitureId;
    }
}
