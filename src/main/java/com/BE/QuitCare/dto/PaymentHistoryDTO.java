package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentHistoryDTO {
    private Long id;
    private Double amountPaid;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private Long userMembershipId;
    private Long accountId; // ID của tài khoản liên quan
}
