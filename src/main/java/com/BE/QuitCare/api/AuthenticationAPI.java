package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.AccountResponse;
import com.BE.QuitCare.dto.LoginRequest;
import com.BE.QuitCare.dto.RegisterRequest;
import com.BE.QuitCare.dto.UpdateProfileRequest;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Api public dùng về tài khoản"
)
@RestController
@RequestMapping("/api/auth")

public class AuthenticationAPI {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequest registerRequest){
        Account newAccount = authenticationService.register(registerRequest);
        return ResponseEntity.ok(newAccount);
    }
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequest loginRequest){
        AccountResponse account = authenticationService.login(loginRequest);
        return ResponseEntity.ok(account);
    }



}
