package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.MessageStatus;
import com.BE.QuitCare.enums.MessageTypeStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MessageNotificationDTO
{
    private Long id;
    private MessageTypeStatus messageTypeStatus;
    private String content;
    private LocalDate sendAt;
    private MessageStatus messageStatus;
    private Long quitProgressId;



}
