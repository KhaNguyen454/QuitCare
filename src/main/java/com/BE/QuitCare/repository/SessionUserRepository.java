package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.SessionUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SessionUserRepository extends JpaRepository<SessionUser,Long>
{
    Optional<SessionUser> findByAccountAndDateAndStart(Account account, LocalDate date, LocalTime start);

    List<SessionUser> findByAccountAndDate(Account account, LocalDate date);

    List<SessionUser> findByAccountAndDateBetweenOrderByDateAscStartAsc(Account account, LocalDate from, LocalDate to);

    List<SessionUser> findByAccountAndDateBetweenAndIsAvailableTrueOrderByDateAscStartAsc(Account account, LocalDate from, LocalDate to);


}
