package com.BE.QuitCare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class SessionUser
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    LocalDate date;

    @ManyToOne
        @JoinColumn(name = "account_id")
    Account account;

    @ManyToOne
    @JoinColumn(name = "session_id")
    Session session;

    boolean isAvailable = true;

    @OneToMany(mappedBy = "sessionUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();



}
