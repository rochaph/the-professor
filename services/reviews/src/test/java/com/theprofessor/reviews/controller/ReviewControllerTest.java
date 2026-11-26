package com.theprofessor.reviews.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.theprofessor.reviews.dto.ReviewResponse;
import com.theprofessor.reviews.entity.ReviewStatus;
import com.theprofessor.reviews.exception.ReviewNotFoundException;
import com.theprofessor.reviews.service.ReviewService;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ReviewService service;

    @Test
    void createReturns201WithLocation() {
        UUID id = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        OffsetDateTime playedAt = OffsetDateTime.now().minusDays(1);

        given(service.request(any())).willReturn(
                new ReviewResponse(id, teamId, matchId, playedAt.toInstant(), ReviewStatus.PENDING));

        String body = String.format("{ \"teamId\": \"%s\", \"matchId\": \"%s\", \"playedAt\": \"%s\" }", teamId,
                matchId, playedAt);

        assertThat(mvc.post().uri("/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/reviews/" + id).bodyJson().extractingPath("$.status")
                .isEqualTo("PENDING");

    }

    @Test
    void createReturns400WhenBodyIsInvalid() {
        String body = "{}";

        assertThat(mvc.post().uri("/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.errors[*].field").asArray()
                .containsExactlyInAnyOrder("teamId", "matchId", "playedAt");

        verifyNoInteractions(service);
    }

    @Test
    void createReturns400WhenMatchIsInTheFuture() {
        String body = String.format("{ \"teamId\": \"%s\", \"matchId\": \"%s\", \"playedAt\": \"%s\" }",
                UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now().plusDays(1));

        assertThat(mvc.post().uri("/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[*].field").asArray()
                .containsExactly("playedAt");

        verifyNoInteractions(service);
    }

    @Test
    void getByIdReturns200WithReview() {
        UUID id = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        OffsetDateTime playedAt = OffsetDateTime.now().minusDays(1);

        given(service.findById(id)).willReturn(
                new ReviewResponse(id, teamId, matchId, playedAt.toInstant(), ReviewStatus.PENDING));

        assertThat(mvc.get().uri("/reviews/{id}", id)).hasStatus(HttpStatus.OK).bodyJson()
                .extractingPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void getByIdReturns404WhenNotFound() {
        UUID id = UUID.randomUUID();

        given(service.findById(id)).willThrow(new ReviewNotFoundException(id));

        assertThat(mvc.get().uri("/reviews/{id}", id)).hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).bodyJson()
                .extractingPath("$.detail").isEqualTo("Review " + id + " not found");
    }

}
