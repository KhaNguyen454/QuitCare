package com.BE.QuitCare.dto.request;

import lombok.Data;

@Data
public class QuitPlanStageUpdateRequest {
    private String week_range;
    private Long targetCigarettes;
    private int durationInWeeks;
    private Boolean markAsCompleted;
}