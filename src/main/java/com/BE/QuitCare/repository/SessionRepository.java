package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SessionRepository extends JpaRepository<Session, Long>
{
    boolean existsByDate(LocalDate date);

}
