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

    public UserRankingDTO(Long userId, String fullName, String username, String avatar, Integer totalPoint) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.avatar = avatar;
        this.totalPoint = totalPoint;
    }

}
