package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.CommunityPost;
import com.BE.QuitCare.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

}
