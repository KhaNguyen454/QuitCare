package com.BE.QuitCare.api;


import com.BE.QuitCare.dto.request.AppointmentRequest;
import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.repository.AppointmentRepository;
import com.BE.QuitCare.service.AppointmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Booking"
)
@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/booking")
public class AppointmentAPI
{
    @Autowired
    private AppointmentService appointmentService;


    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody AppointmentRequest appointmentRequest)
    {
        Appointment appointment = appointmentService.create(appointmentRequest);
        return ResponseEntity.ok().body(appointment);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) {
        appointmentService.confirmAppointment(id);
        return ResponseEntity.ok("Appointment confirmed successfully.");
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok("Appointment cancelled successfully.");
    }



}
