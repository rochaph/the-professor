package com.theprofessor.reviews.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record CreateReviewRequest(@NotNull UUID teamId, @NotNull UUID matchId, @NotNull @PastOrPresent OffsetDateTime playedAt) {
}
