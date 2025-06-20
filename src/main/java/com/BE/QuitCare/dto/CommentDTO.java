package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.CommentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;

    private String content;

    private CommentStatus commentStatus;

    private LocalDateTime createAt = LocalDateTime.now();
}
