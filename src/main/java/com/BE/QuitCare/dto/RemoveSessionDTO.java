package com.BE.QuitCare.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RemoveSessionDTO
{
    private Long accountId;
    private LocalDate date;
}
