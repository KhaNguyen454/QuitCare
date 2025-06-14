package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
