package com.BE.QuitCare.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class FeedbackSession
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    int rating;
    String message;
    private LocalDateTime createAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "session_id")
    Session session;
}
