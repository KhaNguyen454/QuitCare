package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.Gender;
import com.BE.QuitCare.enums.Role;
import lombok.Data;

@Data
public class AccountResponse {
    public String email;
    public String phone;
    public String fullName;
    private String username;
    public Gender gender;
    public Role role;
    public String token;
}