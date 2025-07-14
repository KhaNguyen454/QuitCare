package com.BE.QuitCare.api;

import com.BE.QuitCare.service.GoogleMeetService;
import com.google.api.client.auth.oauth2.Credential;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/google/calendar")
public class GoogleCalendarController {

    private final GoogleMeetService googleMeetService;

    public GoogleCalendarController(GoogleMeetService googleMeetService) {
        this.googleMeetService = googleMeetService;
    }

    @GetMapping("/auth")
    public void authorize(HttpServletResponse response) throws IOException, GeneralSecurityException {
        String authUrl = googleMeetService.getAuthorizationUrl();
        response.sendRedirect(authUrl); // Redirect người dùng đến Google login
    }

    @GetMapping("/callback")
    public String oauth2Callback(@RequestParam("code") String code) {
        googleMeetService.exchangeCodeForToken(code);
        return "Đăng nhập Google Calendar thành công! Bạn có thể thử tạo lịch hẹn lại.";
    }

    @GetMapping("/google/calendar/token")
    public String checkToken() throws IOException, GeneralSecurityException {
        Credential credential = googleMeetService.getStoredCredential();
        if (credential == null) {
            return "Token chưa được tạo!";
        } else {
            return "Đã có token: " + credential.getAccessToken();
        }
    }

}
