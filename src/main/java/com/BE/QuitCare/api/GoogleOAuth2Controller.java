package com.BE.QuitCare.api;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.ResourceNotFoundException;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.service.TokenService;
import com.BE.QuitCare.service.oauth2.CurrentUser;
import com.BE.QuitCare.service.oauth2.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(
        name = "02. Đăng nhập bẳng Google"
)
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class GoogleOAuth2Controller {

    private final AuthenticationRepository authRepo;
    private final TokenService tokenService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public Account getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return authRepo.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }


}
