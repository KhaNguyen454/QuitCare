package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.RegisterSessionDTO;
import com.BE.QuitCare.dto.RemoveSessionDTO;
import com.BE.QuitCare.entity.Session;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.service.SessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "đăng ký lịch làm của bác sĩ"
)
@RestController
@RequestMapping("/api/session")
@SecurityRequirement(name = "api")
public class SessionAPI
{

    @Autowired
    SessionService sessionService;

    @GetMapping("/templates")
    public ResponseEntity<List<Session>> getTemplates() {
        return ResponseEntity.ok(sessionService.getTemplates());
    }

    @DeleteMapping("/remove-day")
    public ResponseEntity<String> removeWorkingDay(@RequestBody RemoveSessionDTO dto) {
        sessionService.removeWorkingDay(dto);
        return ResponseEntity.ok("Đã xin nghỉ thành công cho ngày " + dto.getDate());
    }


}
