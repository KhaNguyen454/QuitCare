package com.BE.QuitCare.dto;

import lombok.Data;

@Data
public class UserRankingDTO
{
    private Long userId;
    private String fullName;
    private String username;
    private String avatar;
    private Integer totalPoint;
}
