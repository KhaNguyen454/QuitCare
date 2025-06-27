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
        appointment.setExpireAt(LocalDateTime.now().plusHours(2));
        appointment.setAccount(currentAccount);
        appointment.setSessionUser(slot);
        appointmentRepository.save(appointment);
        //set slot do thanh da dat
        slot.setAvailable(false);
        return appointment;
    }
    @org.springframework.transaction.annotation.Transactional
    public void confirmAppointment(Long appointmentId) {
        Account currentCoach = authenticationService.getCurentAccount();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn"));

        // Kiểm tra quyền Coach và sở hữu
        if (currentCoach.getRole() != Role.COACH ) {
            throw new SecurityException("Bạn không có quyền xác nhận lịch hẹn này.");
        }

        // Kiểm tra trạng thái
        if (appointment.getStatus() != AppointmentEnum.PENDING) {
            throw new BadRequestException("Lịch hẹn không ở trạng thái chờ xác nhận.");
        }

        // Kiểm tra thời gian hết hạn
        if (appointment.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Lịch hẹn đã hết thời gian chờ xác nhận.");
        }

        appointment.setStatus(AppointmentEnum.COMPLETED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Account currentCoach = authenticationService.getCurentAccount();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy lịch hẹn"));

        if (currentCoach.getRole() != Role.COACH) {
            throw new SecurityException("Bạn không có quyền hủy lịch hẹn này.");
        }

        if (appointment.getStatus() != AppointmentEnum.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy lịch đang chờ xác nhận.");
        }

        appointment.setStatus(AppointmentEnum.CANCELLED);

        // Mở lại slot
        SessionUser slot = appointment.getSessionUser();
        if (slot != null) {
            slot.setAvailable(true);
            sessionUserRepository.save(slot);
        }

        appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelExpiredAppointments() {
        List<Appointment> expiredAppointments = appointmentRepository
                .findAllByStatusAndExpireAtBefore(AppointmentEnum.PENDING, LocalDateTime.now());

        for (Appointment appointment : expiredAppointments) {
            appointment.setStatus(AppointmentEnum.CANCELLED);

            // Nếu có sessionUser thì set lại slot là available
            SessionUser sessionUser = appointment.getSessionUser();
            if (sessionUser != null) {
                sessionUser.setAvailable(true);
                sessionUserRepository.save(sessionUser); // Đừng quên lưu lại
            }

            appointmentRepository.save(appointment);
        }
    }

}
