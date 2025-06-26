package com.BE.QuitCare.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentRequest
{
    long sessionId;
    long coachId;
    LocalDate appointmentDate;
}
