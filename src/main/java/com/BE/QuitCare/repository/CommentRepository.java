package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByCommunityPostId(Long communityPostId);
    Comment findByIdAndAccountId(Long id, Long accountId);
}
