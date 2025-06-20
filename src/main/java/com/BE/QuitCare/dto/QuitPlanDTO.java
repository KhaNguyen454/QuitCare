package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.AddictionLevel;
import com.BE.QuitCare.enums.QuitPlanStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuitPlanDTO {
    private Long id;
    private AddictionLevel addictionLevel;
    private boolean isSystemPlan;
    private LocalDateTime localDateTime;
    private QuitPlanStatus quitPlanStatus;
    private Long accountId; // Để liên kết với Account
    private List<QuitPlanStageDTO> stages; // Bao gồm cả stages khi cần hiển thị chi tiết
}