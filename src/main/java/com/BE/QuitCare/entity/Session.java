package com.BE.QuitCare.entity;


import com.BE.QuitCare.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Session
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    LocalTime start;
    LocalTime end;
    boolean isDelete =false;
    Status status;
    String notes;
    private LocalDateTime createAt = LocalDateTime.now();

    @OneToMany(mappedBy = "session")
    List<SessionUser>  sessionUsers;

    @OneToMany(mappedBy = "session")
    List<FeedbackSession>  feedbackSessions;


}
