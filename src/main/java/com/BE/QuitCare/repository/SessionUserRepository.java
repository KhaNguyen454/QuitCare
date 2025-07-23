package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SessionUserRepository extends JpaRepository<SessionUser,Long>
{
    Optional<SessionUser> findByAccountAndDateAndStart(Account account, LocalDate date, LocalTime start);

    List<SessionUser> findByAccountAndDate(Account account, LocalDate date);

    List<SessionUser> findByAccountAndDateBetweenOrderByDateAscStartAsc(Account account, LocalDate from, LocalDate to);

    @Query("SELECT s FROM SessionUser s WHERE s.account.id = :accountId AND FUNCTION('DATE', s.date) = :date")
    List<SessionUser> findByAccountIdAndDate(@Param("accountId") Long accountId, @Param("date") LocalDate date);


    List<SessionUser> findByLeaveStatus(LeaveStatus status);




}
