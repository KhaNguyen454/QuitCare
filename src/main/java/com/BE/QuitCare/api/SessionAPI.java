package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.RegisterSessionDTO;
import com.BE.QuitCare.entity.Session;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.service.SessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "đăng ký tư vấn"
)
@RestController
@RequestMapping("/api/session")
@SecurityRequirement(name = "api")
public class SessionAPI
{
    @Autowired
    SessionService sessionService;
    @PostMapping
    public void generateSession()
    {
        sessionService.generateSession();
    }
    @GetMapping
    public ResponseEntity getSlots()
    {
        List<Session> sessions = sessionService.get();
        return ResponseEntity.ok(sessions);
    }
    @PostMapping("register")
    public ResponseEntity registerSession(@RequestBody RegisterSessionDTO registerSessionDTO)
    {
        List<SessionUser>  sessionUsers = sessionService.registerSession(registerSessionDTO);
        return ResponseEntity.ok(sessionUsers);
    }
}
