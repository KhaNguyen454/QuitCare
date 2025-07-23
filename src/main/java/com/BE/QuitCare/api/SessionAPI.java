package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.*;
import com.BE.QuitCare.entity.Session;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.service.AuthenticationService;
import com.BE.QuitCare.service.SessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "đăng ký lịch tư vấn của Customer và lịch làm của Coach"
)
@RestController
@RequestMapping("/api/session")
@SecurityRequirement(name = "api")
public class SessionAPI
{

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthenticationService authenticationService;

    @PutMapping("/availability-day")
    public ResponseEntity<String> updateAvailabilityDay(@RequestBody RemoveSessionDTO dto) {
        sessionService.updateAvailabilityDay(dto);
        return ResponseEntity.ok("Đã cập nhật trạng thái nghỉ thành công cho ngày " + dto.getDate());
    }

    @PutMapping("/approve-leave")
    public ResponseEntity<String> approveLeave(@RequestBody ApproveLeaveDTO dto) {
        sessionService.approveCoachLeave(dto);
        return ResponseEntity.ok("Đã xác nhận nghỉ cho coach trong ngày " + dto.getDate());
    }

    @GetMapping("/pending-leave-requests")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<CoachLeaveRequestDTO>> getPendingLeaveRequests() {
        List<CoachLeaveRequestDTO> pendingRequests = sessionService.getPendingLeaveRequests();
        return ResponseEntity.ok(pendingRequests);
    }

    @PutMapping("/cancel-leave")
    public ResponseEntity<?> cancelLeave(@RequestBody ApproveLeaveDTO dto) {
        sessionService.cancelCoachLeave(dto);
        return ResponseEntity.ok("Yêu cầu nghỉ đã bị hủy thành công.");
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

    @GetMapping("/work-stats")
    public ResponseEntity<WorkDayStatsDTO> getWorkDayStats(
            @RequestParam Long coachId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        WorkDayStatsDTO stats = sessionService.getWorkDayStats(coachId, from, to);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/coaches")
    public ResponseEntity<List<CoachInfoDTO>> getAllCoaches() {
        return ResponseEntity.ok(authenticationService.getAllCoaches());
    }






}
