package com.BE.QuitCare.dto.request;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.AccountStatus;
import com.BE.QuitCare.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullname;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    public Account toEntity(String encodedPassword) {
        Account account = new Account();
        account.setEmail(this.email);
        account.setUsername(this.username);
        account.setFullName(this.fullname);
        account.setPassword(encodedPassword);
        account.setRole(Role.GUEST);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreateAt(LocalDateTime.now());
        return account;
    }

}
