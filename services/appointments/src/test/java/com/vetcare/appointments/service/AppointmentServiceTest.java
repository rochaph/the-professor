package com.vetcare.appointments.service;

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

import com.vetcare.appointments.dto.CreateAppointmentRequest;
import com.vetcare.appointments.entity.Appointment;
import com.vetcare.appointments.entity.AppointmentStatus;
import com.vetcare.appointments.exception.AppointmentNotFoundException;
import com.vetcare.appointments.repository.AppointmentRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    AppointmentRepository repository;

    @InjectMocks
    AppointmentService service;

    @Test
    void scheduleSavesAppointmentAsPending() {
        UUID petId = UUID.randomUUID();
        UUID veterinarianId = UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);

        var request = new CreateAppointmentRequest(petId, veterinarianId, scheduledAt);

        given(repository.save(any(Appointment.class))).willAnswer(invocation -> invocation.getArgument(0));

        var response = service.schedule(request);

        ArgumentCaptor<Appointment> saved = ArgumentCaptor.forClass(Appointment.class);

        then(repository).should().save(saved.capture());

        assertThat(saved.getValue().getStatus()).isEqualTo(AppointmentStatus.PENDING);

        assertThat(saved.getValue().getScheduledAt()).isEqualTo(scheduledAt.toInstant());

        assertThat(response.petId()).isEqualTo(petId);
    }

    @Test
    void findByIdReturnsExistingAppointment() {
        UUID appointmentId = UUID.randomUUID();
        UUID petId = UUID.randomUUID();
        UUID veterinarianId = UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);

        var appointment = new Appointment(petId, veterinarianId, scheduledAt.toInstant());

        given(repository.findById(appointmentId)).willReturn(Optional.of(appointment));

        var response = service.findById(appointmentId);

        assertThat(response.petId()).isEqualTo(petId);
        assertThat(response.veterinarianId()).isEqualTo(veterinarianId);
        assertThat(response.scheduledAt()).isEqualTo(scheduledAt.toInstant());
    }

    @Test
    void findByIdThrowsExceptionForNonExistingAppointment() {
        UUID appointmentId = UUID.randomUUID();

        given(repository.findById(appointmentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(appointmentId)).isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining(appointmentId.toString());
    }
}
