package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<PaymentHistory> findByUserMembershipIdOrderByCreatedAtDesc(Long userMembershipId);

    Optional<PaymentHistory> findByVnpTxnRef(String vnpTxnRef);
}
