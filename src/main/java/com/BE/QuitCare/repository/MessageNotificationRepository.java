package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.MessageNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageNotificationRepository extends JpaRepository<MessageNotification, Long >
{

    List<MessageNotification> findByQuitprogress_Id(Long quitProgressId);

    @Query("SELECT m FROM MessageNotification m " +
            "WHERE m.quitprogress.smokingStatus.account.id = :accountId " +
            "ORDER BY m.send_at DESC")
    List<MessageNotification> findAllByAccountId(@Param("accountId") Long accountId);

}
