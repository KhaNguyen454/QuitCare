package com.BE.QuitCare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuitProgressDTO2 {
    private LocalDate startDate;
    private LocalDate endDate;
    private long daysWithoutSmoking;
    private int cigarettesAvoided;
    private int moneySaved;
    private double completionRate;
    private long daysNotTracked;

}
