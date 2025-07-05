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
    private LocalDateTime startDate; // Thêm startDate
    private LocalDateTime endDate; // Thêm endDate
    private QuitPlanStatus quitPlanStatus;
    private Long accountId; // Thêm accountId để tiện trả về
    private List<QuitPlanStageDTO> stages; // Bao gồm danh sách các giai đoạn
}