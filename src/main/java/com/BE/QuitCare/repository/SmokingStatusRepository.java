package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.QuitPlan;
import com.BE.QuitCare.entity.SmokingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmokingStatusRepository extends JpaRepository<SmokingStatus, Long>
{
    Optional<SmokingStatus> findByAccountId(Long accountId);
}
