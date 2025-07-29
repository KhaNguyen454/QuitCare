package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.AccountStatus;
import com.BE.QuitCare.enums.Gender;
import com.BE.QuitCare.enums.Role;
import lombok.Data;

@Data
public class AccountDTO
{
    private Long id;
    private String email;
    private String fullName;
    private String username;
    private Gender gender;
    private Role role;
    private AccountStatus status;
    private String avatar;
    private String description;
}
