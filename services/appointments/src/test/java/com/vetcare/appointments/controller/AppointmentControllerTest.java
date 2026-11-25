package com.vetcare.appointments.controller;

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

import com.vetcare.appointments.dto.AppointmentResponse;
import com.vetcare.appointments.entity.AppointmentStatus;
import com.vetcare.appointments.exception.AppointmentNotFoundException;
import com.vetcare.appointments.service.AppointmentService;

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    AppointmentService service;

    @Test
    void createReturns201WithLocation() {
        UUID id = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        UUID veterinarianId = UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);

        given(service.schedule(any())).willReturn(
                new AppointmentResponse(id, petId, veterinarianId, scheduledAt.toInstant(), AppointmentStatus.PENDING));

        String body = String.format("{ \"petId\": \"%s\", \"veterinarianId\": \"%s\", \"scheduledAt\": \"%s\" }", petId,
                veterinarianId, scheduledAt);

        assertThat(mvc.post().uri("/appointments").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/appointments/" + id).bodyJson().extractingPath("$.status")
                .isEqualTo("PENDING");

    }

    @Test
    void createReturns400WhenBodyIsInvalid() {
        String body = "{}";

        assertThat(mvc.post().uri("/appointments").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.errors[*].field").asArray()
                .containsExactlyInAnyOrder("petId", "veterinarianId", "scheduledAt");

        verifyNoInteractions(service);
    }

    @Test
    void getByIdReturns200WithAppointment() {
        UUID id = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        UUID veterinarianId = UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);

        given(service.findById(id)).willReturn(
                new AppointmentResponse(id, petId, veterinarianId, scheduledAt.toInstant(), AppointmentStatus.PENDING));

        assertThat(mvc.get().uri("/appointments/{id}", id)).hasStatus(HttpStatus.OK).bodyJson()
                .extractingPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void getByIdReturns404WhenNotFound() {
        UUID id = UUID.randomUUID();

        given(service.findById(id)).willThrow(new AppointmentNotFoundException(id));

        assertThat(mvc.get().uri("/appointments/{id}", id)).hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON).bodyJson()
                .extractingPath("$.detail").isEqualTo("Appointment " + id + " not found");
    }

}
