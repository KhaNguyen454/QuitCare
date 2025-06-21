package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.CommentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class CommunityPostDTO {
    private Long id;

    private String title;

    private String description;

    private String image;

    private String category;

    private CommentStatus status;

    private String date;
    private List<CommentDTO> comments;

    private Long accountId;
}
