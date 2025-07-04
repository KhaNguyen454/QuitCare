package com.BE.QuitCare.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentCoachResponseDTO
{
    private String customerName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private String status;
    private String googleMeetLink;
}
