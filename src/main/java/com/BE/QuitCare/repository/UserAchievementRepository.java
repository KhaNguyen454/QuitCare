package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.UserAchievement;
import com.BE.QuitCare.enums.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long>
{
    boolean existsByAccount_IdAndAchievementType(Long accountId, AchievementType type);
    List<UserAchievement> findByAccount_Id(Long accountId);
}

