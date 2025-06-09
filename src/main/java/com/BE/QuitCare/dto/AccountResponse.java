package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.Gender;
import com.BE.QuitCare.enums.Role;
import lombok.Data;

@Data
public class AccountResponse {
    public String email;
    public String fullname;
    public Gender gender;
    public Role role;
    public String token;

}