package com.BE.QuitCare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
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

    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private String label;

    @ManyToOne
        @JoinColumn(name = "account_id")
    Account account;

    boolean isAvailable = true;

    @OneToMany(mappedBy = "sessionUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Appointment> appointments = new ArrayList<>();



}
