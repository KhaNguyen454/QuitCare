package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.RegisterSessionDTO;
import com.BE.QuitCare.dto.RemoveSessionDTO;
import com.BE.QuitCare.dto.SessionUserDTO;
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

    @GetMapping("/working-days")
    public ResponseEntity<List<SessionUserDTO>> getWorkingSessionsForCoach(
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<SessionUserDTO> sessions = sessionService.getWorkingSessionsForCurrentCoach(from, to);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<SessionUserDTO>> getAvailableSlotsForBooking(
            @RequestParam Long coachId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<SessionUserDTO> sessions = sessionService.getAvailableSessionsForBooking(coachId, from, to);
        return ResponseEntity.ok(sessions);
    }





}
