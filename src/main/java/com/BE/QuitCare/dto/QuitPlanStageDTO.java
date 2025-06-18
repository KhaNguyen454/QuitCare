package com.BE.QuitCare.dto;

import lombok.Data;

@Data
public class QuitPlanStageDTO {
    private Long id;
    private int stageNumber;
    private String week_range;
    private Long reductionPercentage; // Có thể là null nếu người dùng tự nhập targetCigarettes
    private Long targetCigarettes;
    private Long quitPlanId; // Để liên kết với QuitPlan
}
