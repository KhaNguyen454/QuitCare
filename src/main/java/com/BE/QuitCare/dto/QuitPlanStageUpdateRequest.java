package com.BE.QuitCare.dto;

import lombok.Data;

@Data
public class QuitPlanStageUpdateRequest {
    private String week_range;
    private Long targetCigarettes; // Cho phép cập nhật targetCigarettes
    // Không cho phép cập nhật stageNumber hay reductionPercentage trực tiếp
}