package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{

}
