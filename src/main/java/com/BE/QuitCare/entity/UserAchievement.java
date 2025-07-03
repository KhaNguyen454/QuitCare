package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.AchievementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserAchievement
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AchievementType achievementType;

    private String description;

    private LocalDateTime achievedAt;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Quitprogress quitprogress;
}
