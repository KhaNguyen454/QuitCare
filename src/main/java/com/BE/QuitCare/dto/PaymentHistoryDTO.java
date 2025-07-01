package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.PaymentMethod;
import com.BE.QuitCare.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentHistoryDTO {
    private Long id;
    private Double amountPaid;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String vnpTxnRef;
    private String vnpOrderInfo;
    private String vnpTransactionNo;
    private String vnpResponseCode;
    private String vnpBankCode;
    private String vnpCardType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userMembershipId;
    private Long accountId; // ID của tài khoản liên quan
}
