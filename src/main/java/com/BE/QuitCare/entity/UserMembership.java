package com.BE.QuitCare.entity;



import com.BE.QuitCare.enums.StatusMembership;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter

public class UserMembership
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    LocalDate date;

    StatusMembership status;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

   @ManyToOne
    @JoinColumn(name ="membership_id")
    MembershipPlan membership;

    @OneToOne(mappedBy = "payment")
    private PaymentHistory paymentHistory;


}
