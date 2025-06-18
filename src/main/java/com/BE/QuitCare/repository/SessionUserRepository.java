package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.SessionUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SessionUserRepository extends JpaRepository<SessionUser,Long>
{
    List<SessionUser> findAccountSessionsByAccountAndDate(Account account, LocalDate date);

}
