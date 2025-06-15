package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    List<UserMembership> findByDeletedFalse();
}
