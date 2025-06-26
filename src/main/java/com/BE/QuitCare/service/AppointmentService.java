package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.request.AppointmentRequest;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.enums.AppointmentEnum;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.AppointmentRepository;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.SessionUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService
{
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    SessionUserRepository sessionUserRepository;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    AuthenticationRepository authenticationRepository;

    @Transactional
    public Appointment create(AppointmentRequest appointmentRequest) {
        Account doctor = authenticationRepository.findById(appointmentRequest.getCoachId()).orElseThrow(()-> new BadRequestException("Coach not found"));

        if(doctor.getRole() != (Role.COACH))
        {
            throw new BadRequestException("Account is not a Coach");
        }


        SessionUser slot =sessionUserRepository.findAccountSlotBySessionIdAndAccountAndDate(
                appointmentRequest.getSessionId(),
                doctor,
                appointmentRequest.getAppointmentDate()
        );
        if(!slot.isAvailable())
        {
            throw new BadRequestException("Slot is not available");
        }

        Account currentAccount = authenticationService.getCurentAccount();
        if (currentAccount.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only customers are allowed to create appointments");
        }

        Appointment appointment=new Appointment();
        appointment.setCreateAt(LocalDate.now());
        appointment.setStatus(AppointmentEnum.PENDING);
        appointment.setAccount(currentAccount);
        appointmentRepository.save(appointment);
        //set slot do thanh da dat
        slot.setAvailable(false);
        return appointment;
    }
}
