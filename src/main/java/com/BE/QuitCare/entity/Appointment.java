package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.AppointmentEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

}
