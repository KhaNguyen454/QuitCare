package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.MembershipPlanDTO;
import com.BE.QuitCare.dto.UserRankingDTO;
import com.BE.QuitCare.dto.response.AccountResponse;
import com.BE.QuitCare.dto.request.LoginRequest;
import com.BE.QuitCare.dto.request.RegisterRequest;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.service.AuthenticationService;
import com.BE.QuitCare.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = " 01. Đăng nhập và đăng ký tài khoản"
)
@RestController
@RequestMapping("/api/auth")

public class AuthenticationAPI {

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    MembershipPlanService service;

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
        List<Account> topUsers = authenticationRepository.findTop10ByRoleOrderByTotalPointDesc(Role.CUSTOMER);
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

    @GetMapping("/api/membership-plans")
    public ResponseEntity<List<MembershipPlanDTO>> getAllPlans() {
        return ResponseEntity.ok(service.getAll());
    }
}
