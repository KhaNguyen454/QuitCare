package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.enums.AppointmentEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{
    List<Appointment> findBySessionUser_Account_IdOrderByCreateAtDesc(Long coachId);

    List<Appointment> findByAccount_IdOrderByCreateAtDesc(Long customerId);

    int countByAccount_IdAndSessionUser_DateBetween(Long accountId, LocalDate startDate, LocalDate endDate);


}
