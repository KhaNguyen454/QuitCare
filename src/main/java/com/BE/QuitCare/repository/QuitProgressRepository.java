package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.entity.SmokingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface QuitProgressRepository extends JpaRepository<Quitprogress, Long>
{
    List<Quitprogress> findTop3BySmokingStatusOrderByDateDesc(SmokingStatus status);

    boolean existsBySmokingStatus_IdAndDate(Long smokingStatusId, LocalDate date);

    List<Quitprogress> findBySmokingStatus_IdOrderByDateDesc(Long smokingStatusId);

}
