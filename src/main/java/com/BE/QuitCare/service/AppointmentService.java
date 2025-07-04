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
import java.time.LocalDateTime;
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
    @Autowired
    SessionService sessionService;

    @Transactional
    public Appointment create(AppointmentRequest appointmentRequest) {
        Account doctor = authenticationRepository.findById(appointmentRequest.getCoachId())
                .orElseThrow(() -> new BadRequestException("Coach not found"));

        if (doctor.getRole() != Role.COACH) {
            throw new BadRequestException("Account is not a Coach");
        }
        sessionService.ensureSessionForCoachOnDate(doctor, appointmentRequest.getAppointmentDate());


        SessionUser slot = sessionUserRepository.findByAccountAndDateAndStart(
                doctor,
                appointmentRequest.getAppointmentDate(),
                appointmentRequest.getStartTime()
        ).orElseThrow(() -> new BadRequestException("Không tìm thấy slot phù hợp"));

        if (!slot.isAvailable()) {
            throw new BadRequestException("Slot is not available");
        }

        Account currentAccount = authenticationService.getCurentAccount();
        if (currentAccount.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only customers are allowed to create appointments");
        }

        Appointment appointment = new Appointment();
        appointment.setCreateAt(LocalDate.now());
        appointment.setStatus(AppointmentEnum.COMPLETED);
        appointment.setAccount(currentAccount);
        appointment.setSessionUser(slot);
        appointment.setGoogleMeetLink(generateGoogleMeetLink());
        appointmentRepository.save(appointment);

        slot.setAvailable(false);
        return appointment;
    }

    private String generateGoogleMeetLink() {
        // Cách đơn giản: tạo chuỗi giả lập như Meet
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
        return "https://meet.google.com/" + uniqueId;
    }



    public List<Appointment> getAppointmentsForCurrentCoach() {
        Account coach = authenticationService.getCurentAccount();

        if (coach == null) {
            throw new SecurityException("Không thể lấy thông tin Coach đang đăng nhập.");
        }

        if (coach.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ Coach mới có thể xem lịch hẹn.");
        }

        // Lấy tất cả lịch hẹn liên quan đến Coach, đã được xác nhận (COMPLETED)
        List<Appointment> appointments = appointmentRepository
                .findBySessionUser_Account_IdOrderByCreateAtDesc(coach.getId());

        return appointments;
    }



}
