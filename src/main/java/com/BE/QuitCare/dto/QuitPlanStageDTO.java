package com.BE.QuitCare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuitPlanStageDTO {
    private Long id;
    private int stageNumber;// Thứ tự của giai đoạn
    private String week_range;// Số tuần trong giai đoạn đó :Từ tuần n đến tuần m
    private Long reductionPercentage; //Cái này là hệ thông đề xuất, Có thể là null nếu người dùng tự nhập targetCigarettes
    private Long targetCigarettes; // Cái này là người dùng nhập
    private Long quitPlanId; // Để liên kết với QuitPlan
}
