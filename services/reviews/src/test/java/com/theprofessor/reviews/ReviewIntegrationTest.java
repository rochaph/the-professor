package com.theprofessor.reviews;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.theprofessor.reviews.entity.Review;
import com.theprofessor.reviews.repository.ReviewRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ReviewIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ReviewRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createReturns201WithLocation() {
        UUID teamId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        OffsetDateTime playedAt = OffsetDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MICROS);

        String body = String.format("{ \"teamId\": \"%s\", \"matchId\": \"%s\", \"playedAt\": \"%s\" }",
                teamId, matchId, playedAt);

        MvcTestResult result = mvc.post().uri("/reviews").contentType(MediaType.APPLICATION_JSON).content(body).exchange();

        assertThat(repository.findAll()).hasSize(1);

        Review saved = repository.findAll().getFirst();

        assertThat(saved.getTeamId()).isEqualTo(teamId);

        assertThat(saved.getMatchId()).isEqualTo(matchId);

        assertThat(saved.getPlayedAt()).isEqualTo(playedAt.toInstant());

        assertThat(result).hasStatus(201).hasHeader("Location", "http://localhost/reviews/" + saved.getId())
                .bodyJson().extractingPath("$.status").isEqualTo("PENDING");

    }
}
