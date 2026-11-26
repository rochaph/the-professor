package com.theprofessor.appointments.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theprofessor.appointments.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID>{

}
