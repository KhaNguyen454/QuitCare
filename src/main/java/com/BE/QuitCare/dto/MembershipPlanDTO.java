package com.BE.QuitCare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipPlanDTO {
    private Long id;
    private String name;
    private Double price;
    private String description;
}
