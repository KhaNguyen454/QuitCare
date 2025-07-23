package com.BE.QuitCare.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApproveLeaveDTO {
    private Long coachId;
    private LocalDate date;
}
