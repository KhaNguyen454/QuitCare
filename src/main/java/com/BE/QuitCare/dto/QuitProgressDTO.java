package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.QuitHealthStatus;
import com.BE.QuitCare.enums.QuitProgressStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class QuitProgressDTO
{
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private LocalDate date;
    private int cigarettes_smoked;
    private QuitHealthStatus quitHealthStatus;
    private int money_saved;
    private QuitProgressStatus quitProgressStatus;

    private Long quitPlanStageId;
    private Long smokingStatusId;
}
