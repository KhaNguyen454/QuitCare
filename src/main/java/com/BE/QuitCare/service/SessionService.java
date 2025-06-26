package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.RegisterSessionDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Session;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.SessionRepository;
import com.BE.QuitCare.repository.SessionUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public List<Session> get()
    {
        return sessionRepository.findAll();
    }
    public List<SessionUser> registerSession(RegisterSessionDTO registerSessionDTO)
    {
        Account account = authenticationRepository.findById(registerSessionDTO.getAccountId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản"));

        //  Chỉ cho phép COACH đăng ký lịch
        if (account.getRole() != Role.COACH) {
            throw new BadRequestException("Chỉ có Coach mới được đăng ký lịch.");
        }

        List<SessionUser>  sessionUsers = new ArrayList<>();
        List<SessionUser> oldAccountSlot= sessionUserRepository.findAccountSessionsByAccountAndDate(account,registerSessionDTO.getDate());

        if(!oldAccountSlot.isEmpty())
        {
            //=> da co lich roi
            throw new BadRequestException("Đã có lịch");
        }

        for(Session session : sessionRepository.findAll())
        {
            SessionUser sessionUser = new SessionUser();
            sessionUser.setSession(session);
            sessionUser.setAccount(account);
            sessionUser.setDate(registerSessionDTO.getDate());
            sessionUsers.add(sessionUser);
        }

        return  sessionUserRepository.saveAll(sessionUsers);
    }

    public void generateSession() {
        //generate tu 7h sang toi 17h
        LocalTime start = LocalTime.of(7, 0);
        LocalTime end = LocalTime.of(17, 0);
        List<Session> sessions = new ArrayList<>();

        while(start.plusMinutes(90).compareTo(end) <= 0) {
            Session session = new Session();
            session.setLable(start.toString());
            session.setStart(start);
            session.setEnd(start.plusMinutes(90));

            sessions.add(session);
            start = start.plusMinutes(30);
        }
        sessionRepository.saveAll(sessions);
    }

}
