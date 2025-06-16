package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    List<MembershipPlan> findByDeletedFalse();
    Optional<MembershipPlan> findByIdAndDeletedFalse(Long id);
}
