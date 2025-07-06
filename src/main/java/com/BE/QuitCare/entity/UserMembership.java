package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.MembershipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class UserMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private MembershipStatus status;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private Account account;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan membershipPlan;

    @OneToMany(mappedBy = "userMembership")
    @JsonIgnore
    private List<PaymentHistory> paymentHistories;

}
