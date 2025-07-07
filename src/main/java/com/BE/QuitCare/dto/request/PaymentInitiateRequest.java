package com.BE.QuitCare.dto.request;


import com.BE.QuitCare.enums.PaymentStatus;
import lombok.Data;
import jakarta.validation.constraints.NotNull; // Thêm validation nếu cần

@Data
public class PaymentInitiateRequest {
    long paymentId;
    long membershipPlanId;
    PaymentStatus paymentStatus;


    // accountId sẽ được lấy từ SecurityContext, không cần truyền từ frontend
    // private Long accountId; // Không cần thiết nếu lấy từ SecurityContext
}
