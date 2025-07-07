package com.BE.QuitCare.dto.request;

import com.BE.QuitCare.enums.QuitPlanStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuitPlanUpdateRequest {
    private Boolean isSystemPlan; // Cho phép thay đổi loại kế hoạch
    private QuitPlanStatus quitPlanStatus; // Cho phép cập nhật trạng thái
}
