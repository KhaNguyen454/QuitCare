package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.QuitPlanStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface QuitPlanStageRepository extends JpaRepository<QuitPlanStage, Long> {
    List<QuitPlanStage> findByQuitPlanIdOrderByStageNumberAsc(Long quitPlanId);

    @Modifying
    @Transactional
    @Query("DELETE FROM QuitPlanStage qps WHERE qps.id = :id")
    void deleteByStageId(Long id);
}
