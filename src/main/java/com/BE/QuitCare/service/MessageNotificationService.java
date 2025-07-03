package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.MessageNotificationDTO;
import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.enums.MessageStatus;
import com.BE.QuitCare.repository.MessageNotificationRepository;
import com.BE.QuitCare.repository.QuitProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageNotificationService
{
    @Autowired
    private MessageNotificationRepository repository;

    @Autowired
    private QuitProgressRepository quitprogressRepository;

    public List<MessageNotificationDTO> getAll() {
        return repository.findAll().stream().map(this::convertToDTO).toList();
    }

    public List<MessageNotificationDTO> getByProgressId(Long progressId) {
        List<MessageNotification> list = repository.findByQuitprogress_Id(progressId);
        return list.stream().map(this::convertToDTO).toList();
    }

    public MessageNotificationDTO create(MessageNotificationDTO dto) {
        Quitprogress quitprogress = quitprogressRepository.findById(dto.getQuitProgressId()).orElse(null);
        if (quitprogress == null) return null;

        MessageNotification entity = convertToEntity(dto, quitprogress);
        MessageNotification saved = repository.save(entity);
        return convertToDTO(saved);
    }

    public MessageNotificationDTO update(Long id, MessageNotificationDTO dto) {
        return repository.findById(id).map(existing -> {
            Quitprogress quitprogress = quitprogressRepository.findById(dto.getQuitProgressId()).orElse(null);
            if (quitprogress == null) return null;

            existing.setMessageTypeStatus(dto.getMessageTypeStatus());
            existing.setContent(dto.getContent());
            existing.setSend_at(dto.getSendAt());
            existing.setMessageStatus(dto.getMessageStatus());
            existing.setQuitprogress(quitprogress);

            return convertToDTO(repository.save(existing));
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

    // ======= Mapping Methods =========

    private MessageNotificationDTO convertToDTO(MessageNotification entity) {
        MessageNotificationDTO dto = new MessageNotificationDTO();
        dto.setId(entity.getId());
        dto.setMessageTypeStatus(entity.getMessageTypeStatus());
        dto.setContent(entity.getContent());
        dto.setSendAt(entity.getSend_at());
        dto.setMessageStatus(entity.getMessageStatus());
        dto.setQuitProgressId(entity.getQuitprogress() != null ? entity.getQuitprogress().getId() : null);
        return dto;
    }

    private MessageNotification convertToEntity(MessageNotificationDTO dto, Quitprogress quitprogress) {
        MessageNotification entity = new MessageNotification();
        entity.setId(dto.getId());
        entity.setMessageTypeStatus(dto.getMessageTypeStatus());
        entity.setContent(dto.getContent());
        entity.setSend_at(dto.getSendAt());
        entity.setMessageStatus(dto.getMessageStatus());
        entity.setQuitprogress(quitprogress);
        return entity;
    }

}
