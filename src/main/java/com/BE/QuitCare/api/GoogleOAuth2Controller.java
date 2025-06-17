package com.BE.QuitCare.api;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.service.TokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(
        name = "Đăng nhập bẳng Google"
)
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class GoogleOAuth2Controller {

    private final AuthenticationRepository authRepo;
    private final TokenService tokenService;

    @PostMapping("/login/success")
    public void oauth2Success(@AuthenticationPrincipal OAuth2User oauthUser,
                              HttpServletResponse response) throws IOException {
        String email = oauthUser.getAttribute("email");

        // Tìm hoặc tạo mới Account
        Account account = authRepo.findAccountByEmail(email);
        if (account == null) {
            account = new Account();
            account.setEmail(email);
            account.setPassword(""); // Không cần password cho OAuth2
            account.setRole(Role.GUEST);
            authRepo.save(account);
        }

        // Tạo JWT token
        String token = tokenService.generateToken(account);

        // Redirect về FE kèm token (có thể thay đổi)
        response.sendRedirect("http://localhost:5173/oauth2-success?token=" + token);
    }

}
