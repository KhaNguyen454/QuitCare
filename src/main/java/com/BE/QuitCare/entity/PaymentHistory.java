package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amountPaid; // Số tiền đã thanh toán (đơn vị: tiền đồng)

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // Trạng thái thanh toán (PENDING, SUCCESS, FAILED, ...)
    
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "membership_id", nullable = false)
    @JsonIgnore
    private UserMembership userMembership;

     @ManyToOne
     @JoinColumn(name = "account_id", nullable = false)
     @JsonIgnore
     private Account account;
}