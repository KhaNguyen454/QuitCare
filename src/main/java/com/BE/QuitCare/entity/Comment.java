package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.CommentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private CommentStatus commentStatus;

    private LocalDateTime createAt = LocalDateTime.now();


    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "post_id")
    CommunityPost communityPost;
}
