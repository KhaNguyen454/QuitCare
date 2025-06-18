package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.QuitPlanStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuitPlanStageRepository extends JpaRepository<QuitPlanStage, Long> {
    List<QuitPlanStage> findByQuitPlanIdOrderByStageNumberAsc(Long quitPlanId);
}
