package com.BE.QuitCare.dto;

import lombok.Data;

@Data
public class AppointmentResponseDTO2
{
    private Long id;
    private String status;
    private String googleMeetLink;
    private String createAt;

    // Coach Info (trích từ sessionUser.account)
    private Long coachId;
    private String coachName;
    private String coachEmail;

    // Customer Info (từ appointment.account)
    private Long customerId;
    private String customerName;
    private String customerEmail;

    // Session Info
    private String sessionDate;
    private String sessionStart;
    private String sessionEnd;

    // Membership Info (từ appointment.userMembership)
    private Long membershipId;
    private String membershipPlanName;
    private String membershipStart;
    private String membershipEnd;
}
