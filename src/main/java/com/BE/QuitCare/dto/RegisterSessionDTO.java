package com.BE.QuitCare.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterSessionDTO
{
    LocalDate date;
    long accountId;
}
