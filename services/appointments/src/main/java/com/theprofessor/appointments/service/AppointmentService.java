package com.theprofessor.appointments.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theprofessor.appointments.dto.AppointmentResponse;
import com.theprofessor.appointments.dto.CreateAppointmentRequest;
import com.theprofessor.appointments.entity.Appointment;
import com.theprofessor.appointments.exception.AppointmentNotFoundException;
import com.theprofessor.appointments.repository.AppointmentRepository;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public AppointmentResponse schedule(CreateAppointmentRequest request) {
        Appointment appointment = new Appointment(
            request.petId(),
            request.veterinarianId(),
            request.scheduledAt().toInstant()
        );

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return AppointmentResponse.from(savedAppointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException(id));
        return AppointmentResponse.from(appointment);
    }
}
