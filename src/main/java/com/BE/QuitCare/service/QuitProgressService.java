package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.QuitProgressDTO;
import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.entity.QuitPlanStage;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.enums.MessageTypeStatus;
import com.BE.QuitCare.enums.QuitHealthStatus;
import com.BE.QuitCare.enums.QuitProgressStatus;
import com.BE.QuitCare.repository.MessageNotificationRepository;
import com.BE.QuitCare.repository.QuitPlanStageRepository;
import com.BE.QuitCare.repository.QuitProgressRepository;
import com.BE.QuitCare.repository.SmokingStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private QuitProgressRepository quitProgressRepository;

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
        quitprogress.setPoint(point);

        MessageTypeStatus typeStatus;

        // Base NOTIFICATION1 or 2
        if (point < 0) {
            typeStatus = MessageTypeStatus.NOTIFICATION1;
        } else {
            typeStatus = MessageTypeStatus.NOTIFICATION2;
        }

        // ✅ Get last 3 days of Quitprogress records for this user/smokingStatus
        List<Quitprogress> last3Days = quitProgressRepository
                .findTop3BySmokingStatusOrderByDateDesc(quitprogress.getSmokingStatus());

        // Check symptom counts
        Map<QuitHealthStatus, Long> symptomCounts = last3Days.stream()
                .filter(p -> p.getQuitHealthStatus() != null)
                .collect(Collectors.groupingBy(Quitprogress::getQuitHealthStatus, Collectors.counting()));

        if (!symptomCounts.isEmpty()) {
            if (symptomCounts.size() > 2 && last3Days.size() == 3) {
                typeStatus = MessageTypeStatus.NOTIFICATION4;
            } else if (symptomCounts.values().stream().anyMatch(count -> count == 3)) {
                typeStatus = MessageTypeStatus.NOTIFICATION3;
            }
        }

        // Get quit reason
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

        // Generate content
        String content = switch (typeStatus) {
            case NOTIFICATION1 -> "Cảnh báo: Số điếu hút hôm nay tăng! Bạn đã quyết định bỏ thuốc " + quitReasonDisplay + ".";
            case NOTIFICATION2 -> "Tuyệt vời! Bạn đã giảm số điếu hút. Hãy nhớ bạn đã bắt đầu " + quitReasonDisplay + ".";
            case NOTIFICATION3 -> "Bạn đã có một triệu chứng kéo dài 3 ngày. Hãy đặt lịch với huấn luyện viên để kiểm tra sức khỏe.";
            case NOTIFICATION4 -> "Bạn đã có hơn 2 triệu chứng khác nhau trong 3 ngày gần đây. Nên hẹn gặp huấn luyện viên sớm.";
        };

        MessageNotification notification = new MessageNotification();
        notification.setQuitprogress(quitprogress);
        notification.setSend_at(LocalDate.now());
        notification.setMessageTypeStatus(typeStatus);
        notification.setContent(content);

        return notification;
    }




}
