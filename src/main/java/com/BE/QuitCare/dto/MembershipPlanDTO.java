package com.BE.QuitCare.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class
MembershipPlanDTO {
    private Long id;
    private String name;
    private Long price;
    private String description;
    private Long durationInDays;
}
