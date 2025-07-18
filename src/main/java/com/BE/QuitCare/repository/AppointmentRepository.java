package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.enums.AppointmentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{
    List<Appointment> findBySessionUser_Account_IdOrderByCreateAtDesc(Long coachId);

    List<Appointment> findByAccount_IdOrderByCreateAtDesc(Long customerId);

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.account.id = :accountId " +
            "AND a.sessionUser.date BETWEEN :start AND :end")
    int countByAccountAndDateInMembership(@Param("accountId") Long accountId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    int countByUserMembership_Id(Long membershipId);



}
