package com.BE.QuitCare.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest
{
    private Long coachId;
    private LocalDate appointmentDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", example = "09:00")
    private LocalTime start;
}
