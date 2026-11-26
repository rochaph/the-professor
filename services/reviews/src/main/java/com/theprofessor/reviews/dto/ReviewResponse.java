package com.theprofessor.reviews.dto;

import java.time.Instant;
import java.util.UUID;

import com.theprofessor.reviews.entity.Review;
import com.theprofessor.reviews.entity.ReviewStatus;

public record ReviewResponse(UUID id, UUID teamId, UUID matchId, Instant playedAt, ReviewStatus status) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getTeamId(),
            review.getMatchId(),
            review.getPlayedAt(),
            review.getStatus()
        );
    }
}
