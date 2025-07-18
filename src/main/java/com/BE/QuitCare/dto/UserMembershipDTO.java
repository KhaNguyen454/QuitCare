package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.MembershipStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMembershipDTO {
    //private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private MembershipStatus status;
    private Long planId;
}
