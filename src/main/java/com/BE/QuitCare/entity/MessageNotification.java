package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.MessageStatus;
import com.BE.QuitCare.enums.MessageTypeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class MessageNotification
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    MessageTypeStatus messageTypeStatus;
    String content;
    LocalDate send_at;
    @Enumerated(EnumType.STRING)
    MessageStatus messageStatus;

   @ManyToOne
    @JoinColumn(name = "quitProgress_id")
    private  Quitprogress quitprogress;
}
