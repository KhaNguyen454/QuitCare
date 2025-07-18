package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    List<UserMembership> findAllByDeletedFalse();
    Optional<UserMembership> findByIdAndDeletedFalse(Long id);

    Optional<UserMembership> findFirstByAccount_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(
            Long accountId, LocalDateTime start, LocalDateTime end);

    List<UserMembership> findByStatusAndEndDateBefore(MembershipStatus status, java.time.LocalDateTime endDate);


}
