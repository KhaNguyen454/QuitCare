package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.QuitProgressDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.entity.QuitPlanStage;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.enums.*;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    private AuthenticationRepository authenticationRepository;
    @Autowired
    UserAchievementService userAchievementService;

    public List<Quitprogress> getAll() {
        return repository.findAll();
    }

    public Optional<Quitprogress> getById(Long id) {
        return repository.findById(id);
    }

    public Quitprogress create(QuitProgressDTO dto) {

        Account coach = authenticationService.getCurentAccount();

        if (coach.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Chỉ Customer mới có thể khai báo.");
        }
        // ===== CHẶN trùng ngày =====
        if (dto.getSmokingStatusId() != null && dto.getDate() != null) {
            boolean exists = repository.existsBySmokingStatus_IdAndDate(dto.getSmokingStatusId(), dto.getDate());
            if (exists) {
                throw new IllegalArgumentException("Bạn đã khai báo tiến trình trong ngày này rồi.");
            }
        }

        Quitprogress quitprogress = new Quitprogress();
        quitprogress.setDate(dto.getDate());
        quitprogress.setCigarettes_smoked(dto.getCigarettes_smoked());
        quitprogress.setQuitHealthStatus(dto.getQuitHealthStatus());
        quitprogress.setQuitProgressStatus(dto.getQuitProgressStatus());

        // ===== Lấy QuitPlanStage =====
        QuitPlanStage stage = null;
        if (dto.getQuitPlanStageId() != null) {
            stage = stageRepository.findById(dto.getQuitPlanStageId()).orElse(null);
            quitprogress.setQuitPlanStage(stage);
        }

        // ===== Lấy SmokingStatus =====
        if (dto.getSmokingStatusId() != null) {
            smokingStatusRepository.findById(dto.getSmokingStatusId())
                    .ifPresent(quitprogress::setSmokingStatus);
        }

        // ===== TÍNH POINT & MONEY_SAVED =====
        int referenceValue = 0;
        if (stage != null && stage.getTargetCigarettes() != null) {
            referenceValue = stage.getTargetCigarettes().intValue();
        }

        int rawPoint = referenceValue - quitprogress.getCigarettes_smoked();
        int point = Math.max(rawPoint, 0) * 10;
        quitprogress.setPoint(point);

        int moneySaved = Math.max(rawPoint, 0) * 1000;
        quitprogress.setMoney_saved(moneySaved);

        // ===== LƯU Quitprogress =====
        Quitprogress saved = repository.save(quitprogress);

        // ===== GỘP logic sinh thông báo luôn ở đây =====
        List<MessageNotification> notifications = generateNotifications(saved);
        messageNotificationRepository.saveAll(notifications);



        Account user = quitprogress.getSmokingStatus().getAccount();
        user.setTotalPoint(user.getTotalPoint() + quitprogress.getPoint());
        authenticationRepository.save(user);

        // Check thành tựu
        userAchievementService.checkAndGenerate(user, quitprogress);
        return saved;
    }



    public Quitprogress update(Long id, QuitProgressDTO dto) {
        Account user = authenticationService.getCurentAccount();
        if (user.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Chỉ Customer mới có thể cập nhật tiến trình.");
        }

        return repository.findById(id).map(existing -> {
            existing.setDate(dto.getDate());
            existing.setCigarettes_smoked(dto.getCigarettes_smoked());
            existing.setQuitHealthStatus(dto.getQuitHealthStatus());
            existing.setQuitProgressStatus(dto.getQuitProgressStatus());

            // ===== Cập nhật QuitPlanStage =====
            if (dto.getQuitPlanStageId() != null) {
                QuitPlanStage stage = stageRepository.findById(dto.getQuitPlanStageId()).orElse(null);
                existing.setQuitPlanStage(stage);
            }

            // ===== Cập nhật SmokingStatus =====
            if (dto.getSmokingStatusId() != null) {
                smokingStatusRepository.findById(dto.getSmokingStatusId())
                        .ifPresent(existing::setSmokingStatus);
            }

            // ===== TÍNH LẠI POINT & MONEY_SAVED =====
            int referenceValue = 0;
            QuitPlanStage stage = existing.getQuitPlanStage();
            if (stage != null && stage.getTargetCigarettes() != null) {
                referenceValue = stage.getTargetCigarettes().intValue();
            }

            int rawPoint = referenceValue - existing.getCigarettes_smoked();
            int point = Math.max(rawPoint, 0) * 10;
            int moneySaved = Math.max(rawPoint, 0) * 1000;

            existing.setPoint(point);
            existing.setMoney_saved(moneySaved);

            // ===== CẬP NHẬT TỔNG ĐIỂM USER (nếu có liên kết SmokingStatus) =====
            if (existing.getSmokingStatus() != null) {
                Account acc = existing.getSmokingStatus().getAccount();
                acc.setTotalPoint(acc.getTotalPoint() + point); // cộng thêm điểm mới
                authenticationRepository.save(acc);
            }

            return repository.save(existing);
        }).orElseThrow(() -> new BadRequestException("Không tìm thấy tiến trình cần cập nhật."));
    }


    public List<Quitprogress> autoCheckMissedDays(Long smokingStatusId) {
        List<Quitprogress> createdMissed = new ArrayList<>();

        // Tìm ngày gần nhất mà user đã khai báo
        List<Quitprogress> allProgress = quitProgressRepository
                .findBySmokingStatus_IdOrderByDateDesc(smokingStatusId);

        if (allProgress.isEmpty()) return createdMissed;

        LocalDate lastRecordedDate = allProgress.get(0).getDate();
        LocalDate today = LocalDate.now();

        for (LocalDate d = lastRecordedDate.plusDays(1); d.isBefore(today); d = d.plusDays(1)) {
            boolean exists = quitProgressRepository.existsBySmokingStatus_IdAndDate(smokingStatusId, d);
            if (!exists) {
                Quitprogress missed = new Quitprogress();
                missed.setDate(d);
                missed.setCigarettes_smoked(0);
                missed.setQuitProgressStatus(QuitProgressStatus.MISSED);
                missed.setQuitHealthStatus(null);
                missed.setPoint(0);
                missed.setMoney_saved(0);

                smokingStatusRepository.findById(smokingStatusId)
                        .ifPresent(missed::setSmokingStatus);

                createdMissed.add(repository.save(missed));
            }
        }

        return createdMissed;
    }




    public List<MessageNotification> generateNotifications(Quitprogress quitprogress) {
        List<MessageNotification> notifications = new ArrayList<>();

        // 1. Tính điểm
        int referenceValue = 0;
        QuitPlanStage stage = quitprogress.getQuitPlanStage();
        if (stage != null && stage.getTargetCigarettes() != null) {
            referenceValue = stage.getTargetCigarettes().intValue();
        }

        int point = referenceValue - quitprogress.getCigarettes_smoked();

        // 2. Lý do bỏ thuốc
        String quitReasonDisplay = "Không rõ lý do.";
        if (quitprogress.getSmokingStatus() != null && quitprogress.getSmokingStatus().getQuitReasons() != null) {
            quitReasonDisplay = switch (quitprogress.getSmokingStatus().getQuitReasons()) {
                case Improving_health -> "vì sức khỏe";
                case Family_loved_ones -> "vì gia đình";
                case Financial_pressure -> "vì áp lực tài chính";
                case Feeling_tired_of_addiction -> "vì cảm thấy mệt mỏi vì nghiện";
                case Being_banned_from_smoking_at_work_home -> "vì bị cấm hút tại nơi làm việc / gia đình";
                case Wanting_to_set_an_example_for_children -> "vì muốn làm gương cho con cái";
            };
        }

        // 3. Tạo thông báo NOTIFICATION2 nếu giảm được thuốc
        if (point > 0) {
            MessageNotification n2 = new MessageNotification();
            n2.setQuitprogress(quitprogress);
            n2.setSend_at(LocalDate.now());
            n2.setMessageTypeStatus(MessageTypeStatus.NOTIFICATION2);
            n2.setMessageStatus(MessageStatus.NORMAL);
            n2.setContent("Tuyệt vời! Bạn đã giảm số điếu hút. Hãy nhớ bạn đã bắt đầu " + quitReasonDisplay + ".");
            notifications.add(n2);
        }
        if (point < 0) {
            MessageNotification n1 = new MessageNotification();
            n1.setQuitprogress(quitprogress);
            n1.setSend_at(LocalDate.now());
            n1.setMessageTypeStatus(MessageTypeStatus.NOTIFICATION1);
            n1.setMessageStatus(MessageStatus.WARNING);
            n1.setContent("Cảnh báo: Số điếu hút hôm nay tăng! Bạn đã quyết định bỏ thuốc " + quitReasonDisplay + ".");
            notifications.add(n1);
        }


        // 4. Lấy 3 ngày gần nhất của người dùng
        List<Quitprogress> last3Days = quitProgressRepository
                .findTop3BySmokingStatusOrderByDateDesc(quitprogress.getSmokingStatus());

        // 5. Tính số triệu chứng và số lần xuất hiện
        Map<QuitHealthStatus, Long> symptomCounts = last3Days.stream()
                .filter(p -> p.getQuitHealthStatus() != null)
                .collect(Collectors.groupingBy(Quitprogress::getQuitHealthStatus, Collectors.counting()));

        if (!symptomCounts.isEmpty() && last3Days.size() == 3) {
            // NOTIFICATION4: hơn 2 triệu chứng khác nhau trong 3 ngày
            if (symptomCounts.size() > 2) {
                MessageNotification n4 = new MessageNotification();
                n4.setQuitprogress(quitprogress);
                n4.setSend_at(LocalDate.now());
                n4.setMessageTypeStatus(MessageTypeStatus.NOTIFICATION4);
                n4.setMessageStatus(MessageStatus.WARNING);
                n4.setContent("Bạn đã có hơn 2 triệu chứng khác nhau trong 3 ngày gần đây. Nên hẹn gặp huấn luyện viên sớm.");
                notifications.add(n4);
            }

            // NOTIFICATION3: 1 triệu chứng duy nhất lặp lại 3 ngày
            if (symptomCounts.values().stream().anyMatch(count -> count == 3)) {
                MessageNotification n3 = new MessageNotification();
                n3.setQuitprogress(quitprogress);
                n3.setSend_at(LocalDate.now());
                n3.setMessageTypeStatus(MessageTypeStatus.NOTIFICATION3);
                n3.setMessageStatus(MessageStatus.WARNING);
                n3.setContent("Bạn đã có một triệu chứng kéo dài 3 ngày. Hãy đặt lịch với huấn luyện viên để kiểm tra sức khỏe.");
                notifications.add(n3);
            }
        }

        return notifications;
    }





}
