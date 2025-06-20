package com.BE.QuitCare.dto.request;

import com.BE.QuitCare.enums.QuitPlanStatus;
import lombok.Data;

@Data
public class QuitPlanUpdateRequest {
    private Boolean isSystemPlan; // Sử dụng Boolean để có thể xác định nếu không được gửi lên
    private QuitPlanStatus quitPlanStatus;
    // Có thể thêm các trường khác nếu người dùng được phép cập nhật
}
