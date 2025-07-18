package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.entity.SmokingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuitProgressRepository extends JpaRepository<Quitprogress, Long>
{
    List<Quitprogress> findTop3BySmokingStatusOrderByDateDesc(SmokingStatus status);

    boolean existsBySmokingStatus_IdAndDate(Long smokingStatusId, LocalDate date);

    List<Quitprogress> findBySmokingStatus_IdOrderByDateDesc(Long smokingStatusId);

    @Query("SELECT q FROM Quitprogress q WHERE q.quitPlanStage.id = :stageId AND q.smokingStatus.account.id = :userId")
    List<Quitprogress> findByStageAndUser(@Param("stageId") Long stageId, @Param("userId") Long userId);

}
