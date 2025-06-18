package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.CommentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String image;

    private String category;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    private String date;

    @OneToMany(mappedBy = "communityPost")
            @JsonIgnore
    List<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account account;

    @Column(nullable = false)
    private Boolean deleted = false;
}
