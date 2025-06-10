package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullname;
    private String username;
    private Gender gender;

}
