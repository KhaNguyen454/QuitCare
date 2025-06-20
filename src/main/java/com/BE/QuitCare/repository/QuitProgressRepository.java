package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.entity.SmokingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuitProgressRepository extends JpaRepository<Quitprogress, Long>
{
    List<Quitprogress> findTop3BySmokingStatusOrderByDateDesc(SmokingStatus status);

}
