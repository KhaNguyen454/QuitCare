package com.BE.QuitCare.dto.request;


import lombok.Data;
import jakarta.validation.constraints.NotNull; // Thêm validation nếu cần

@Data
public class PaymentInitiateRequest {
    @NotNull(message = "ID gói thành viên không được để trống")
    private Long membershipPlanId;
    // accountId sẽ được lấy từ SecurityContext, không cần truyền từ frontend
    // private Long accountId; // Không cần thiết nếu lấy từ SecurityContext
}
