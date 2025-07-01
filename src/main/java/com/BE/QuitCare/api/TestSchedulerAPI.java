package com.BE.QuitCare.api;


import com.BE.QuitCare.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestSchedulerAPI
{
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/cancel-expired")
    public ResponseEntity<String> cancelExpiredManually() {
        appointmentService.cancelExpiredAppointments();
        return ResponseEntity.ok("Expired appointments canceled");
    }

}
