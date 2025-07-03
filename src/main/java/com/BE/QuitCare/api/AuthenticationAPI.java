package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.UserRankingDTO;
import com.BE.QuitCare.dto.response.AccountResponse;
import com.BE.QuitCare.dto.request.LoginRequest;
import com.BE.QuitCare.dto.request.RegisterRequest;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Api public dùng về tài khoản"
)
@RestController
@RequestMapping("/api/auth")

public class AuthenticationAPI {

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    AuthenticationRepository authenticationRepository;

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
    @GetMapping("/ranking")
    public ResponseEntity<List<UserRankingDTO>> getRanking() {
        List<Account> topUsers = authenticationRepository.findTop10ByOrderByTotalPointDesc();
        List<UserRankingDTO> result = topUsers.stream()
                .map(a -> new UserRankingDTO(
                        a.getId(),
                        a.getFullName(),
                        a.getUsername(),
                        a.getAvatar(),   
                        a.getTotalPoint()))
                .toList();

        return ResponseEntity.ok(result);
    }



}
