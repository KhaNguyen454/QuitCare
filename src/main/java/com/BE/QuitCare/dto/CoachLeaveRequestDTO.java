package com.BE.QuitCare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoachLeaveRequestDTO {
    private Long coachId;
    private LocalDate date;
}