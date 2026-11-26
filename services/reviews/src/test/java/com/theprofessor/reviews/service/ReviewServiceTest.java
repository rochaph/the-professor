package com.theprofessor.reviews.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theprofessor.reviews.dto.CreateReviewRequest;
import com.theprofessor.reviews.entity.Review;
import com.theprofessor.reviews.entity.ReviewStatus;
import com.theprofessor.reviews.exception.ReviewNotFoundException;
import com.theprofessor.reviews.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository repository;

    @InjectMocks
    ReviewService service;

    @Test
    void requestSavesReviewAsPending() {
        UUID teamId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        OffsetDateTime playedAt = OffsetDateTime.now().minusDays(1);

        var request = new CreateReviewRequest(teamId, matchId, playedAt);

        given(repository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));

        var response = service.request(request);

        ArgumentCaptor<Review> saved = ArgumentCaptor.forClass(Review.class);

        then(repository).should().save(saved.capture());

        assertThat(saved.getValue().getStatus()).isEqualTo(ReviewStatus.PENDING);

        assertThat(saved.getValue().getPlayedAt()).isEqualTo(playedAt.toInstant());

        assertThat(response.teamId()).isEqualTo(teamId);
    }

    @Test
    void findByIdReturnsExistingReview() {
        UUID reviewId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        OffsetDateTime playedAt = OffsetDateTime.now().minusDays(1);

        var review = new Review(teamId, matchId, playedAt.toInstant());

        given(repository.findById(reviewId)).willReturn(Optional.of(review));

        var response = service.findById(reviewId);

        assertThat(response.teamId()).isEqualTo(teamId);
        assertThat(response.matchId()).isEqualTo(matchId);
        assertThat(response.playedAt()).isEqualTo(playedAt.toInstant());
    }

    @Test
    void findByIdThrowsExceptionForNonExistingReview() {
        UUID reviewId = UUID.randomUUID();

        given(repository.findById(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(reviewId)).isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining(reviewId.toString());
    }
}
