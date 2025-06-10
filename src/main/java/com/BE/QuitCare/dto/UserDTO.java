package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.Gender;
import com.BE.QuitCare.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserDTO
{
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String email;
    public String phone;
    public String fullName;
    private String username;
    public Gender gender;
    public Role role;


}
