package com.BE.QuitCare.api;


import com.BE.QuitCare.dto.request.AppointmentRequest;
import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.repository.AppointmentRepository;
import com.BE.QuitCare.service.AppointmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
