package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.QuitPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuitPlanRepository extends JpaRepository<QuitPlan, Long> {
    Optional<QuitPlan> findByAccountId(Long accountId);
}
