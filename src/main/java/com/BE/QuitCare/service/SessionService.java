package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.RegisterSessionDTO;
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
    AuthenticationRepository authenticationRepository;
    @Autowired
    SessionUserRepository sessionUserRepository;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AppointmentRepository appointmentRepository;


    public List<Session> get()
    {
        return sessionRepository.findAll();
    }
    public List<SessionUser> registerSession(RegisterSessionDTO registerSessionDTO) {
        Account account = authenticationRepository.findById(registerSessionDTO.getAccountId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));

        if (account.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ Coach mới được đăng ký.");
        }

        LocalDate date = registerSessionDTO.getDate();
        List<SessionUser> oldSlots = sessionUserRepository.findAccountSessionsByAccountAndDate(account, date);
        if (!oldSlots.isEmpty()) {
            throw new BadRequestException("Đã có lịch trong ngày này.");
        }

        //  Chỉ lấy session đã được tạo đúng ngày đó
        List<Session> sessions = sessionRepository.findAllByDate(date);
        if (sessions.isEmpty()) {
            throw new BadRequestException("Chưa có session nào được tạo cho ngày " + date);
        }

        List<SessionUser> sessionUsers = new ArrayList<>();
        for (Session session : sessions) {
            SessionUser sessionUser = new SessionUser();
            sessionUser.setSession(session);
            sessionUser.setAccount(account);
            sessionUser.setDate(date);
            sessionUsers.add(sessionUser);
        }

        return sessionUserRepository.saveAll(sessionUsers);
    }


    public void generateSession(LocalDate date) {
        // Kiểm tra nếu đã generate rồi trong ngày được nhập
        if (sessionRepository.existsByDate(date)) {
            throw new BadRequestException("Đã tạo lịch cho ngày này rồi.");
        }

        LocalTime start = LocalTime.of(7, 0);
        LocalTime end = LocalTime.of(17, 0);
        List<Session> sessions = new ArrayList<>();

        while (start.plusMinutes(90).compareTo(end) <= 0) {
            Session session = new Session();
            session.setLable(start.toString());
            session.setStart(start);
            session.setEnd(start.plusMinutes(90));
            session.setDate(date); //  dùng ngày được nhập

            sessions.add(session);
            start = start.plusMinutes(30);
        }

        sessionRepository.saveAll(sessions);
    }


}
