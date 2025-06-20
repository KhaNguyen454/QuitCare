package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.StatusPayment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentHistory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    double amount_paid;
    LocalDate payment_date;
    StatusPayment  status_payment;
    StatusPaymentMethod status_payment_method;

    private LocalDateTime createAt = LocalDateTime.now();
    private LocalDateTime updateAt;


    @OneToOne
    @JoinColumn(name = "usermembership_id", unique = true)
    private  UserMembership payment;
}
