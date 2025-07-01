package com.BE.QuitCare.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(name = "date")
    private LocalDate date;
    String lable;
    LocalTime start;
    LocalTime end;
    boolean isDelete =false;

    @OneToMany(mappedBy = "session")
    @JsonIgnore
    List<SessionUser>  sessionUsers;

    @OneToMany(mappedBy = "session")
    List<FeedbackSession>  feedbackSessions;


}
