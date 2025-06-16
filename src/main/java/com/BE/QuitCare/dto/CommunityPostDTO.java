package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.CommentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityPostDTO {
    private Long id;
    private String content;

    private CommentStatus commentStatus;

    private LocalDateTime createAt;
}
