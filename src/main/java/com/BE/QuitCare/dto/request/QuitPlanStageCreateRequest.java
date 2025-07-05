package com.BE.QuitCare.dto.request;

import lombok.Data;

@Data
public class QuitPlanStageCreateRequest {
    private Long quitPlanId; // ID của kế hoạch cai nghiện mà giai đoạn này thuộc về
    private int stageNumber;
    private String week_range;
    private Long targetCigarettes;
    private int durationInWeeks; //trường này để người dùng có thể nhập thời lượng
}