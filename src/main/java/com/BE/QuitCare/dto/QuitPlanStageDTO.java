package com.BE.QuitCare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuitPlanStageDTO {
    private Long id;
    private int stageNumber;
    private String week_range;
    private Long reductionPercentage;
    private Long targetCigarettes;
    private LocalDateTime completionDate; // Thêm completionDate
    private Long quitPlanId; // Thêm quitPlanId để tiện trả về
}
