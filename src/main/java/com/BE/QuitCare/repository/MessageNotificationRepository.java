package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.MessageNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageNotificationRepository extends JpaRepository<MessageNotification, Long >
{

    List<MessageNotification> findByQuitprogress_Id(Long quitProgressId);

}
