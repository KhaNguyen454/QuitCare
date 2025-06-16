package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.CommentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private CommentStatus commentStatus;

    private LocalDateTime createAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "communityPost")
            @JsonIgnore
    List<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account account;
}
