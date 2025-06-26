package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Appointment;
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
        Account doctor = authenticationRepository.findById(appointmentRequest.getStaffId()).orElseThrow(()-> new BadRequestException("Doctor not found"));

        if(doctor.getRole().equals(Role.DOCTOR))
        {
            throw new BadRequestException("Account is not a doctor");
        }


        AccountSlot slot=accountSlotRepository.findAccountSlotBySlotIdAndAccountAndDate(
                appointmentRequest.getSlotId(),
                doctor,
                appointmentRequest.getAppointmentDate()
        );
        if(!slot.isAvailable())
        {
            throw new BadRequestException("Slot is not available");
        }


        List<Medicine> services= medicineServiceRepository.findByIdIn(appointmentRequest.getServiceId());

        Account currentAccount = authenticationService.getCurentAccount();

        Appointment appointment=new Appointment();
        appointment.setCreateAt(LocalDate.now());
        appointment.setStatus(AppointmentEnum.PENDING);
        appointment.setAccount(currentAccount);
        appointment.setMedicines(services);
        appointmentRepository.save(appointment);
        //set slot do thanh da dat
        slot.setAvailable(false);

        return appointment;
    }
}
