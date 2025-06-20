package com.BE.QuitCare.dto;

import lombok.Data;

@Data
public class QuitPlanStageCreateRequest {
    private int stageNumber;
    private String week_range;
    // reductionPercentage không cần nhập, hệ thống tính hoặc để trống
    private Long targetCigarettes; // Người dùng nhập nếu isSystemPlan = false
    private Long quitPlanId; // Để xác định QuitPlan mà stage này thuộc về
}