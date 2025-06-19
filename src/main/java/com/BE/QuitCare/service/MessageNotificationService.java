package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.enums.MessageStatus;
import com.BE.QuitCare.repository.MessageNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageNotificationService
{
    @Autowired
    private MessageNotificationRepository repository;

    public List<MessageNotification> getAll() {
        return repository.findAll();
    }

    public Optional<MessageNotification> getById(Long id) {
        return repository.findById(id);
    }

    public MessageNotification create(MessageNotification notification) {
        return repository.save(notification);
    }

    public MessageNotification update(Long id, MessageNotification updated) {
        return repository.findById(id).map(existing -> {
            existing.setMessageTypeStatus(updated.getMessageTypeStatus());
            existing.setContent(updated.getContent());
            existing.setSend_at(updated.getSend_at());
            existing.setQuitprogress(updated.getQuitprogress());
            return repository.save(existing);
        }).orElse(null);
    }

    public boolean markAsDeleted(Long id) {
        return repository.findById(id).map(notification -> {
            if (notification.getMessageStatus() != MessageStatus.DELETED) {
                notification.setMessageStatus(MessageStatus.DELETED);
                repository.save(notification);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
