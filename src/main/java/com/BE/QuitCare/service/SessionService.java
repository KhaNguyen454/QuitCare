package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.RegisterSessionDTO;
import com.BE.QuitCare.dto.RemoveSessionDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Appointment;
import com.BE.QuitCare.entity.Session;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.enums.AppointmentEnum;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.AppointmentRepository;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.SessionRepository;
import com.BE.QuitCare.repository.SessionUserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SessionService
{

    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    SessionUserRepository sessionUserRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;

    public List<Session> getTemplates() {
        return sessionRepository.findAll();
    }

    public List<SessionUser> registerSession(RegisterSessionDTO dto) {
        Account account = authenticationRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));

        if (account.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ Coach mới được đăng ký.");
        }

        LocalDate date = dto.getDate();
        List<SessionUser> old = sessionUserRepository.findAccountSessionsByAccountAndDate(account, date);
        if (!old.isEmpty()) {
            throw new BadRequestException("Đã có lịch trong ngày này.");
        }

        List<Session> templates = sessionRepository.findAll();
        if (templates.isEmpty()) {
            throw new BadRequestException("Chưa có khung giờ mẫu.");
        }

        List<SessionUser> sessionUsers = new ArrayList<>();
        for (Session template : templates) {
            SessionUser su = new SessionUser();
            su.setAccount(account);
            su.setDate(date);
            su.setStart(template.getStart());
            su.setEnd(template.getEnd());
            su.setLabel(template.getLabel());
            sessionUsers.add(su);
        }

        return sessionUserRepository.saveAll(sessionUsers);
    }

    @PostConstruct
    public void initTemplates() {
        if (sessionRepository.count() == 0) {
            List<Session> templates = new ArrayList<>();
            LocalTime start = LocalTime.of(7, 0);
            LocalTime end = LocalTime.of(17, 0);

            while (start.plusMinutes(60).compareTo(end) <= 0) {
                Session template = new Session();
                template.setLabel(start.toString());
                template.setStart(start);
                template.setEnd(start.plusMinutes(60));
                templates.add(template);

                start = start.plusMinutes(90); // nghỉ 30 phút
            }

            sessionRepository.saveAll(templates);
        }
    }

    @Transactional
    public void removeSession(RemoveSessionDTO dto) {
        Account account = authenticationRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));

        if (account.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ Coach mới được hủy session.");
        }

        SessionUser sessionUser = sessionUserRepository
                .findByAccountAndDateAndStart(account, dto.getDate(), dto.getStartTime())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy session phù hợp."));

        if (!sessionUser.getAppointments().isEmpty()) {
            throw new BadRequestException("Không thể hủy session đã có cuộc hẹn.");
        }

        sessionUserRepository.delete(sessionUser);
    }


}
