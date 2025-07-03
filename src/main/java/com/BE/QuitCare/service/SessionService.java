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
    public void removeWorkingDay(RemoveSessionDTO dto) {
        Account account = authenticationRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));

        if (account.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ Coach mới được hủy lịch làm.");
        }

        List<SessionUser> sessions = sessionUserRepository.findByAccountAndDate(account, dto.getDate());
        if (sessions.isEmpty()) {
            throw new BadRequestException("Không có lịch làm nào trong ngày này để hủy.");
        }

        for (SessionUser su : sessions) {
            if (!su.getAppointments().isEmpty()) {
                throw new BadRequestException("Không thể xin nghỉ nếu đã có lịch hẹn trong ngày này.");
            }
        }

        sessionUserRepository.deleteAll(sessions);
    }

    public void ensureSessionForCoachOnDate(Account coach, LocalDate date) {
        List<SessionUser> sessions = sessionUserRepository.findByAccountAndDate(coach, date);
        if (!sessions.isEmpty()) return;

        List<Session> templates = sessionRepository.findAll();
        List<SessionUser> newSessions = new ArrayList<>();

        for (Session template : templates) {
            SessionUser su = new SessionUser();
            su.setAccount(coach);
            su.setDate(date);
            su.setStart(template.getStart());
            su.setEnd(template.getEnd());
            su.setLabel(template.getLabel());
            newSessions.add(su);
        }

        sessionUserRepository.saveAll(newSessions);
    }



}
