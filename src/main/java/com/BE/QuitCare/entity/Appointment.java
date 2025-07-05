package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.AppointmentEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Appointment
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    LocalDate createAt;
    @Enumerated(EnumType.STRING)

    AppointmentEnum status;

    @Column(name = "google_meet_link")
    private String googleMeetLink;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @ManyToOne
    @JoinColumn(name = "session_user_id")
    private SessionUser sessionUser;
}
