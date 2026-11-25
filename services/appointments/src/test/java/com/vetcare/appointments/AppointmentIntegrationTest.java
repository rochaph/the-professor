package com.vetcare.appointments;

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

import com.vetcare.appointments.entity.Appointment;
import com.vetcare.appointments.repository.AppointmentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class AppointmentIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    AppointmentRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createReturns201WithLocation() {
        UUID petID = UUID.randomUUID();
        UUID veterinarianId = UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MICROS);

        String body = String.format("{ \"petId\": \"%s\", \"veterinarianId\": \"%s\", \"scheduledAt\": \"%s\" }",
                petID, veterinarianId, scheduledAt);

        MvcTestResult result = mvc.post().uri("/appointments").contentType(MediaType.APPLICATION_JSON).content(body).exchange();

        assertThat(repository.findAll()).hasSize(1);

        Appointment saved = repository.findAll().getFirst();

        assertThat(saved.getPetId()).isEqualTo(petID);

        assertThat(saved.getVeterinarianId()).isEqualTo(veterinarianId);

        assertThat(saved.getScheduledAt()).isEqualTo(scheduledAt.toInstant());

        assertThat(result).hasStatus(201).hasHeader("Location", "http://localhost/appointments/" + saved.getId())
                .bodyJson().extractingPath("$.status").isEqualTo("PENDING");

    }
}
