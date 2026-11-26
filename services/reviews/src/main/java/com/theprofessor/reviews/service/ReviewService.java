package com.theprofessor.reviews.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theprofessor.reviews.dto.CreateReviewRequest;
import com.theprofessor.reviews.dto.ReviewResponse;
import com.theprofessor.reviews.entity.Review;
import com.theprofessor.reviews.exception.ReviewNotFoundException;
import com.theprofessor.reviews.repository.ReviewRepository;


@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public ReviewResponse request(CreateReviewRequest request) {
        Review review = new Review(
            request.teamId(),
            request.matchId(),
            request.playedAt().toInstant()
        );

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.from(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse findById(UUID id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));
        return ReviewResponse.from(review);
    }
}
