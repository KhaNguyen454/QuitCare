package com.BE.QuitCare.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest
{
    private Long coachId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
}
