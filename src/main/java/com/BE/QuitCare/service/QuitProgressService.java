package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.QuitProgressDTO;
import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.entity.QuitPlanStage;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.enums.MessageTypeStatus;
import com.BE.QuitCare.enums.QuitProgressStatus;
import com.BE.QuitCare.repository.MessageNotificationRepository;
import com.BE.QuitCare.repository.QuitPlanStageRepository;
import com.BE.QuitCare.repository.QuitProgressRepository;
import com.BE.QuitCare.repository.SmokingStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class QuitProgressService
{
    @Autowired
    private QuitProgressRepository repository;
    @Autowired
    private MessageNotificationRepository messageNotificationRepository;
    @Autowired
    QuitPlanStageRepository stageRepository;
    @Autowired
    SmokingStatusRepository smokingStatusRepository;

    public List<Quitprogress> getAll() {
        return repository.findAll();
    }

    public Optional<Quitprogress> getById(Long id) {
        return repository.findById(id);
    }

    public Quitprogress create(QuitProgressDTO dto) {
        Quitprogress quitprogress = new Quitprogress();
        quitprogress.setDate(dto.getDate());
        quitprogress.setCigarettes_smoked(dto.getCigarettes_smoked());
        quitprogress.setMoney_saved(dto.getMoney_saved());
        quitprogress.setQuitHealthStatus(dto.getQuitHealthStatus());
        quitprogress.setQuitProgressStatus(dto.getQuitProgressStatus());

        // Set QuitPlanStage
        if (dto.getQuitPlanStageId() != null) {
            stageRepository.findById(dto.getQuitPlanStageId())
                    .ifPresent(quitprogress::setQuitPlanStage);
        }

        // Set SmokingStatus
        if (dto.getSmokingStatusId() != null) {
            smokingStatusRepository.findById(dto.getSmokingStatusId())
                    .ifPresent(quitprogress::setSmokingStatus);
        }

        // Save and generate notification
        Quitprogress saved = repository.save(quitprogress);
        MessageNotification notification = generateNotification(saved);
        messageNotificationRepository.save(notification);

        return saved;
    }


    public Quitprogress update(Long id, Quitprogress updated) {
        return repository.findById(id).map(existing -> {
            existing.setDate(updated.getDate());
            existing.setCigarettes_smoked(updated.getCigarettes_smoked());
            existing.setQuitHealthStatus(updated.getQuitHealthStatus());
            existing.setMoney_saved(updated.getMoney_saved());
            existing.setQuitProgressStatus(updated.getQuitProgressStatus());
            existing.setPoint(updated.getPoint());
            existing.setQuitPlanStage(updated.getQuitPlanStage());
            return repository.save(existing);
        }).orElse(null);
    }

    public boolean markAsMissed(Long id) {
        return repository.findById(id).map(progress -> {
            if (progress.getQuitProgressStatus() != QuitProgressStatus.MISSED) {
                progress.setQuitProgressStatus(QuitProgressStatus.MISSED);
                repository.save(progress);
                return true;
            }
            return false;
        }).orElse(false);
    }


    public MessageNotification generateNotification(Quitprogress quitprogress) {
        int referenceValue = 0;

        QuitPlanStage stage = quitprogress.getQuitPlanStage();
        if (stage != null) {
            if (stage.getReductionPercentage() != null) {
                referenceValue = stage.getReductionPercentage().intValue();
            } else if (stage.getTargetCigarettes() != null) {
                referenceValue = stage.getTargetCigarettes().intValue();
            }
        }

        int point = referenceValue - quitprogress.getCigarettes_smoked();
        quitprogress.setPoint(point); // Optional if you want to persist this

        MessageTypeStatus typeStatus = point < 0
                ? MessageTypeStatus.NOTIFICATION1
                : MessageTypeStatus.NOTIFICATION2;

        String quitReasonDisplay = "Không rõ lý do.";
        if (quitprogress.getSmokingStatus() != null && quitprogress.getSmokingStatus().getQuitReasons() != null) {
            quitReasonDisplay = switch (quitprogress.getSmokingStatus().getQuitReasons()) {
                case HEALTH -> "vì sức khỏe";
                case FAMILY -> "vì gia đình";
                case MONEY -> "vì tiết kiệm tiền";
                case SOCIAL -> "vì xã hội";
                case OTHER -> "vì lý do khác";
            };
        }

        //  Final notification message
        String content = switch (typeStatus) {
            case NOTIFICATION1 -> "Cảnh báo: Số điếu hút hôm nay tăng! Bạn đã quyết định bỏ thuốc " + quitReasonDisplay + ".";
            case NOTIFICATION2 -> "Tuyệt vời! Bạn đã giảm số điếu hút. Hãy nhớ bạn đã bắt đầu " + quitReasonDisplay + ".";
            default -> "Thông báo.";
        };

        MessageNotification notification = new MessageNotification();
        notification.setQuitprogress(quitprogress);
        notification.setSend_at(LocalDate.now());
        notification.setMessageTypeStatus(typeStatus);
        notification.setContent(content);

        return notification;
    }



}
