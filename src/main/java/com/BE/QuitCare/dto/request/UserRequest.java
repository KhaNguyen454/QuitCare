package com.BE.QuitCare.dto.request;


import com.BE.QuitCare.enums.Gender;
import lombok.Data;

@Data
public class UserRequest
{
    public String fullName;
    private String username;
    public Gender gender;

}
