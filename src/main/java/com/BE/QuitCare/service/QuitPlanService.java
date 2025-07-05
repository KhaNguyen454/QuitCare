 package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.*;
import com.BE.QuitCare.dto.request.QuitPlanCreateRequest;
import com.BE.QuitCare.dto.request.QuitPlanStageCreateRequest;
import com.BE.QuitCare.dto.request.QuitPlanStageUpdateRequest;
import com.BE.QuitCare.dto.request.QuitPlanUpdateRequest;
import com.BE.QuitCare.entity.*;
import com.BE.QuitCare.enums.AddictionLevel;
import com.BE.QuitCare.enums.QuitPlanStatus;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.QuitPlanRepository;
import com.BE.QuitCare.repository.QuitPlanStageRepository;
import com.BE.QuitCare.repository.SmokingStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuitPlanService {

    @Autowired
    private QuitPlanRepository quitPlanRepository;

    @Autowired
    private QuitPlanStageRepository quitPlanStageRepository;

    @Autowired
    private SmokingStatusRepository smokingStatusRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- Các thao tác với QuitPlan ---

    @Transactional
    public QuitPlanDTO createQuitPlan(Long accountId, QuitPlanCreateRequest request) {
        Account account = authenticationRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + accountId));

        if (quitPlanRepository.findByAccountId(accountId).isPresent()) {
            throw new IllegalArgumentException("Tài khoản đã có một kế hoạch cai nghiện đang tồn tại (nháp hoặc hoạt động).");
        }

        SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản ID: " + accountId + ". Vui lòng điền thông tin tình trạng hút thuốc trước."));

        QuitPlan quitPlan = new QuitPlan();
        quitPlan.setAccount(account);
        quitPlan.setSystemPlan(request.isSystemPlan());
        quitPlan.setStartDate(null);
        quitPlan.setEndDate(null);
        quitPlan.setQuitPlanStatus(QuitPlanStatus.DRAFT);

        quitPlan.setAddictionLevel(calculateAddictionLevel(smokingStatus));

        QuitPlan savedQuitPlan = quitPlanRepository.save(quitPlan);

        if (request.isSystemPlan()) {
            generateSystemQuitPlanStages(savedQuitPlan, smokingStatus.getCigarettes_per_day());
        }

        QuitPlanDTO dto = modelMapper.map(savedQuitPlan, QuitPlanDTO.class);
        dto.setAccountId(accountId);
        dto.setStages(savedQuitPlan.getStages().stream()
                .map(stage -> modelMapper.map(stage, QuitPlanStageDTO.class))
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Đặt ngày bắt đầu cho kế hoạch cai nghiện.
     * Kế hoạch phải ở trạng thái DRAFT. Sau khi đặt, trạng thái sẽ chuyển sang ACTIVE.
     * Đồng thời, tính toán và đặt endDate dự kiến dựa trên tổng thời lượng các giai đoạn.
     * @param accountId ID của tài khoản.
     * @param quitPlanId ID của kế hoạch cai nghiện.
     * @param startDate Ngày bắt đầu mới.
     * @return QuitPlanDTO đã cập nhật.
     * @throws EntityNotFoundException nếu không tìm thấy kế hoạch.
     * @throws SecurityException nếu kế hoạch không thuộc về tài khoản.
     * @throws IllegalArgumentException nếu kế hoạch không ở trạng thái DRAFT hoặc startDate không hợp lệ.
     */
    @Transactional
    public QuitPlanDTO setQuitPlanStartDate(Long accountId, Long quitPlanId, LocalDateTime startDate) {
        QuitPlan quitPlan = quitPlanRepository.findById(quitPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + quitPlanId));

        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền cập nhật kế hoạch cai nghiện này.");
        }

        if (quitPlan.getQuitPlanStatus() != QuitPlanStatus.DRAFT) {
            throw new IllegalArgumentException("Chỉ có thể đặt ngày bắt đầu cho kế hoạch ở trạng thái NHÁP.");
        }
        if (startDate == null || startDate.isBefore(LocalDateTime.now().minusDays(1))) {
            throw new IllegalArgumentException("Ngày bắt đầu không hợp lệ. Phải là ngày hiện tại hoặc tương lai gần.");
        }

        quitPlan.setStartDate(startDate);
        quitPlan.setQuitPlanStatus(QuitPlanStatus.ACTIVE);

        // Tính toán endDate dự kiến dựa trên tổng thời lượng của các giai đoạn
        long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
        if (totalWeeks > 0) {
            quitPlan.setEndDate(startDate.plusWeeks(totalWeeks));
        } else {
            // Nếu không có giai đoạn nào, endDate có thể là startDate (hoặc null nếu bạn muốn)
            quitPlan.setEndDate(startDate);
        }

        QuitPlan savedQuitPlan = quitPlanRepository.save(quitPlan);
        QuitPlanDTO dto = modelMapper.map(savedQuitPlan, QuitPlanDTO.class);
        dto.setAccountId(accountId);
        dto.setStages(savedQuitPlan.getStages().stream()
                .map(stage -> modelMapper.map(stage, QuitPlanStageDTO.class))
                .collect(Collectors.toList()));
        return dto;
    }


    public QuitPlanDTO getQuitPlanByAccountId(Long accountId) {
        QuitPlan quitPlan = quitPlanRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện cho tài khoản ID: " + accountId));

        QuitPlanDTO dto = modelMapper.map(quitPlan, QuitPlanDTO.class);
        dto.setAccountId(accountId);
        dto.setStages(quitPlan.getStages().stream()
                .map(stage -> modelMapper.map(stage, QuitPlanStageDTO.class))
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public QuitPlanDTO updateQuitPlan(Long accountId, Long quitPlanId, QuitPlanUpdateRequest request) {
        QuitPlan quitPlan = quitPlanRepository.findById(quitPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + quitPlanId));

        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền cập nhật kế hoạch cai nghiện này.");
        }

        if (quitPlan.getQuitPlanStatus() == QuitPlanStatus.COMPLETED || quitPlan.getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể cập nhật kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        if (request.getQuitPlanStatus() != null) {
            quitPlan.setQuitPlanStatus(request.getQuitPlanStatus());
        }

        // Cập nhật ngày bắt đầu nếu được cung cấp (chỉ khi kế hoạch là DRAFT hoặc ACTIVE)
        if (request.getStartDate() != null) {
            if (quitPlan.getQuitPlanStatus() == QuitPlanStatus.COMPLETED || quitPlan.getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
                throw new IllegalArgumentException("Không thể thay đổi ngày bắt đầu cho kế hoạch đã hoàn thành hoặc đã hủy.");
            }
            if (request.getStartDate().isBefore(LocalDateTime.now().minusDays(1))) {
                throw new IllegalArgumentException("Ngày bắt đầu không hợp lệ. Phải là ngày hiện tại hoặc tương lai gần.");
            }
            quitPlan.setStartDate(request.getStartDate());
            if (quitPlan.getQuitPlanStatus() == QuitPlanStatus.DRAFT) {
                quitPlan.setQuitPlanStatus(QuitPlanStatus.ACTIVE);
            }
            // Cập nhật lại endDate dự kiến nếu startDate thay đổi
            long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
            if (totalWeeks > 0) {
                quitPlan.setEndDate(request.getStartDate().plusWeeks(totalWeeks));
            } else {
                quitPlan.setEndDate(request.getStartDate());
            }
        }


        if (request.getIsSystemPlan() != null && quitPlan.isSystemPlan() != request.getIsSystemPlan()) {
            if (!quitPlan.isSystemPlan() && request.getIsSystemPlan()) {
                SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(accountId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản ID: " + accountId));
                quitPlanStageRepository.deleteAll(quitPlan.getStages());
                quitPlan.getStages().clear();
                generateSystemQuitPlanStages(quitPlan, smokingStatus.getCigarettes_per_day());
                quitPlan.setSystemPlan(true);
            } else if (quitPlan.isSystemPlan() && !request.getIsSystemPlan()) {
                quitPlanStageRepository.deleteAll(quitPlan.getStages());
                quitPlan.getStages().clear();
                quitPlan.setSystemPlan(false);
            }
            // Sau khi thay đổi loại kế hoạch, cập nhật lại endDate nếu startDate đã có
            if (quitPlan.getStartDate() != null) {
                long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
                if (totalWeeks > 0) {
                    quitPlan.setEndDate(quitPlan.getStartDate().plusWeeks(totalWeeks));
                } else {
                    quitPlan.setEndDate(quitPlan.getStartDate());
                }
            }
        }
        QuitPlan savedQuitPlan = quitPlanRepository.save(quitPlan);
        QuitPlanDTO dto = modelMapper.map(savedQuitPlan, QuitPlanDTO.class);
        dto.setAccountId(accountId);
        dto.setStages(savedQuitPlan.getStages().stream()
                .map(stage -> modelMapper.map(stage, QuitPlanStageDTO.class))
                .collect(Collectors.toList()));
        return dto;
    }

    public void deleteQuitPlan(Long accountId, Long quitPlanId) {
        QuitPlan quitPlan = quitPlanRepository.findById(quitPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + quitPlanId));

        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền xóa kế hoạch cai nghiện này.");
        }
        quitPlanRepository.delete(quitPlan);
    }

    // --- Các thao tác với QuitPlanStage ---

    @Transactional
    public QuitPlanStageDTO createQuitPlanStage(Long accountId, QuitPlanStageCreateRequest request) {
        QuitPlan quitPlan = quitPlanRepository.findById(request.getQuitPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + request.getQuitPlanId()));

        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền thêm giai đoạn vào kế hoạch cai nghiện này.");
        }

        if (quitPlan.isSystemPlan()) {
            throw new IllegalArgumentException("Không thể thêm giai đoạn vào kế hoạch hệ thống.");
        }
        if (quitPlan.getQuitPlanStatus() == QuitPlanStatus.COMPLETED ||
                quitPlan.getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể thêm vào kế hoạch đã hoàn thành hoặc đã hủy.");
        }

        // Validate input
        if (request.getTargetCigarettes() == null) {
            throw new IllegalArgumentException("Trường 'targetCigarettes' không được để trống.");
        }
        if (request.getStageNumber() <= 0) {
            throw new IllegalArgumentException("Stage number phải lớn hơn 0.");
        }
        if (request.getDurationInWeeks() <= 0) { // Đảm bảo thời lượng giai đoạn hợp lệ
            throw new IllegalArgumentException("Thời lượng giai đoạn phải lớn hơn 0 tuần.");
        }

        // Kiểm tra tính tuần tự của stageNumber
        // Lấy lại danh sách stages để đảm bảo tính nhất quán với DB
        List<QuitPlanStage> existingStages = quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(quitPlan.getId());
        Optional<QuitPlanStage> lastStage = existingStages.stream()
                .max(Comparator.comparingInt(QuitPlanStage::getStageNumber));

        if (lastStage.isPresent() && request.getStageNumber() != lastStage.get().getStageNumber() + 1) {
            throw new IllegalArgumentException("Stage number phải lớn hơn giai đoạn cuối cùng hiện có một đơn vị.");
        } else if (!lastStage.isPresent() && request.getStageNumber() != 1) {
            throw new IllegalArgumentException("Giai đoạn đầu tiên phải có stage number là 1.");
        }


        QuitPlanStage stage = new QuitPlanStage();
        stage.setStageNumber(request.getStageNumber());
        stage.setWeek_range(request.getWeek_range());
        stage.setTargetCigarettes(request.getTargetCigarettes());
        stage.setDurationInWeeks(request.getDurationInWeeks()); // Set thời lượng giai đoạn
        stage.setQuitPlan(quitPlan);

        calculateUserDefinedReductionPercentage(quitPlan, stage);

        if (stage.getReductionPercentage() == null) {
            stage.setReductionPercentage(0L);
        }

        QuitPlanStage savedStage = quitPlanStageRepository.save(stage);

        // Cập nhật lại endDate dự kiến của QuitPlan sau khi thêm stage mới
        if (quitPlan.getStartDate() != null) {
            long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
            if (totalWeeks > 0) {
                quitPlan.setEndDate(quitPlan.getStartDate().plusWeeks(totalWeeks));
            } else {
                quitPlan.setEndDate(quitPlan.getStartDate());
            }
            quitPlanRepository.save(quitPlan); // Lưu lại QuitPlan sau khi cập nhật endDate
        }

        QuitPlanStageDTO dto = modelMapper.map(savedStage, QuitPlanStageDTO.class);
        dto.setQuitPlanId(quitPlan.getId());
        return dto;
    }

    public List<QuitPlanStageDTO> getQuitPlanStages(Long accountId, Long quitPlanId) {
        QuitPlan quitPlan = quitPlanRepository.findById(quitPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + quitPlanId));

        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền xem các giai đoạn của kế hoạch cai nghiện này.");
        }

        return quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(quitPlanId)
                .stream()
                .map(stage -> {
                    QuitPlanStageDTO dto = modelMapper.map(stage, QuitPlanStageDTO.class);
                    dto.setQuitPlanId(quitPlanId);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public QuitPlanStageDTO updateQuitPlanStage(Long accountId, Long stageId, QuitPlanStageUpdateRequest request) {
        QuitPlanStage stage = quitPlanStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giai đoạn kế hoạch cai nghiện với ID: " + stageId));

        if (!stage.getQuitPlan().getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền cập nhật giai đoạn kế hoạch cai nghiện này.");
        }

        if (stage.getQuitPlan().isSystemPlan()) {
            throw new IllegalArgumentException("Không thể cập nhật trực tiếp các giai đoạn của kế hoạch cai nghiện do hệ thống tạo.");
        }
        if (stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.COMPLETED || stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể cập nhật các giai đoạn của kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        // Cập nhật các trường nếu được cung cấp trong request
        if (request.getWeek_range() != null) {
            stage.setWeek_range(request.getWeek_range());
        }
        if (request.getTargetCigarettes() != null) {
            stage.setTargetCigarettes(request.getTargetCigarettes());
        }
        if (request.getDurationInWeeks() > 0) { // Cập nhật thời lượng giai đoạn
            stage.setDurationInWeeks(request.getDurationInWeeks());
        }

        // Xử lý khi đánh dấu giai đoạn hoàn thành
        if (request.getMarkAsCompleted() != null && request.getMarkAsCompleted()) {
            if (stage.getCompletionDate() == null) {
                stage.setCompletionDate(LocalDateTime.now());
                // Kiểm tra xem đây có phải là giai đoạn cuối cùng không
                List<QuitPlanStage> allStages = quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(stage.getQuitPlan().getId());
                Optional<QuitPlanStage> lastStage = allStages.stream()
                        .max(Comparator.comparingInt(QuitPlanStage::getStageNumber));

                if (lastStage.isPresent() && lastStage.get().getId().equals(stage.getId())) {
                    // Đây là giai đoạn cuối cùng, cập nhật endDate cho QuitPlan
                    QuitPlan quitPlan = stage.getQuitPlan();
                    if (quitPlan.getStartDate() == null) {
                        throw new IllegalStateException("Kế hoạch chưa có ngày bắt đầu. Vui lòng đặt ngày bắt đầu trước khi hoàn thành giai đoạn cuối.");
                    }
                    // Tính tổng thời lượng của kế hoạch dựa trên các giai đoạn đã hoàn thành
                    long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
                    quitPlan.setEndDate(quitPlan.getStartDate().plusWeeks(totalWeeks)); // Đặt endDate dựa trên tổng thời lượng
                    quitPlan.setQuitPlanStatus(QuitPlanStatus.COMPLETED);
                    quitPlanRepository.save(quitPlan);
                }
            }
        }

        calculateUserDefinedReductionPercentage(stage.getQuitPlan(), stage);

        QuitPlanStage savedStage = quitPlanStageRepository.save(stage);

        // Sau khi cập nhật stage, cập nhật lại endDate dự kiến của QuitPlan nếu cần
        QuitPlan quitPlan = savedStage.getQuitPlan();
        if (quitPlan.getStartDate() != null && quitPlan.getQuitPlanStatus() == QuitPlanStatus.ACTIVE) {
            long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
            if (totalWeeks > 0) {
                quitPlan.setEndDate(quitPlan.getStartDate().plusWeeks(totalWeeks));
            } else {
                quitPlan.setEndDate(quitPlan.getStartDate());
            }
            quitPlanRepository.save(quitPlan);
        }


        QuitPlanStageDTO dto = modelMapper.map(savedStage, QuitPlanStageDTO.class);
        dto.setQuitPlanId(savedStage.getQuitPlan().getId());
        return dto;
    }

    public void deleteQuitPlanStage(Long accountId, Long stageId) {
        QuitPlanStage stage = quitPlanStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giai đoạn kế hoạch cai nghiện với ID: " + stageId));

        if (!stage.getQuitPlan().getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền xóa giai đoạn kế hoạch cai nghiện này.");
        }

        if (stage.getQuitPlan().isSystemPlan()) {
            throw new IllegalArgumentException("Không thể xóa trực tiếp các giai đoạn của kế hoạch cai nghiện do hệ thống tạo.");
        }
        if (stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.COMPLETED || stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể xóa các giai đoạn của kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        quitPlanStageRepository.delete(stage);

        // Sau khi xóa stage, cập nhật lại endDate dự kiến của QuitPlan nếu cần
        QuitPlan quitPlan = stage.getQuitPlan();
        if (quitPlan.getStartDate() != null && quitPlan.getQuitPlanStatus() == QuitPlanStatus.ACTIVE) {
            long totalWeeks = calculateQuitPlanTotalDurationInWeeks(quitPlan);
            if (totalWeeks > 0) {
                quitPlan.setEndDate(quitPlan.getStartDate().plusWeeks(totalWeeks));
            } else {
                quitPlan.setEndDate(quitPlan.getStartDate());
            }
            quitPlanRepository.save(quitPlan);
        }
    }

    // --- Phương thức hỗ trợ ---

    private AddictionLevel calculateAddictionLevel(SmokingStatus smokingStatus) {
        int timeToFirstCigaretteScore;
        switch (smokingStatus.getTimeToFirstCigarette()) {
            case LESS_THAN_5_MIN:
                timeToFirstCigaretteScore = 3;
                break;
            case BETWEEN_6_AND_30_MIN:
                timeToFirstCigaretteScore = 2;
                break;
            case BETWEEN_31_AND_60_MIN:
                timeToFirstCigaretteScore = 1;
                break;
            case MORE_THAN_60_MIN:
            default:
                timeToFirstCigaretteScore = 0;
                break;
        }

        int cigarettesPerDayScore;
        if (smokingStatus.getCigarettes_per_day() <= 10) {
            cigarettesPerDayScore = 0;
        } else if (smokingStatus.getCigarettes_per_day() <= 20) {
            cigarettesPerDayScore = 1;
        } else if (smokingStatus.getCigarettes_per_day() <= 30) {
            cigarettesPerDayScore = 2;
        } else {
            cigarettesPerDayScore = 3;
        }

        int totalScore = timeToFirstCigaretteScore + cigarettesPerDayScore;

        if (totalScore >= 0 && totalScore <= 2) {
            return AddictionLevel.LOW;
        } else if (totalScore >= 3 && totalScore <= 4) {
            return AddictionLevel.MEDIUM;
        } else {
            return AddictionLevel.HIGH;
        }
    }

    /**
     * Tạo các giai đoạn kế hoạch cai thuốc tự động cho kế hoạch hệ thống.
     * @param quitPlan Kế hoạch cai thuốc.
     * @param initialCigarettesPerDay Số điếu thuốc ban đầu mỗi ngày.
     */
    private void generateSystemQuitPlanStages(QuitPlan quitPlan, int initialCigarettesPerDay) {
        long currentCigarettes = initialCigarettesPerDay;
        int stageNumber = 1;
        int defaultDurationPerStageWeeks = 4; // Mỗi giai đoạn hệ thống mặc định 4 tuần

        if (quitPlan.getStages() != null) {
            quitPlanStageRepository.deleteAll(quitPlan.getStages());
            quitPlan.getStages().clear();
        }

        while (currentCigarettes > 0) {
            QuitPlanStage stage = new QuitPlanStage();
            stage.setQuitPlan(quitPlan);
            stage.setStageNumber(stageNumber);
            stage.setWeek_range("Tuần " + ((stageNumber - 1) * defaultDurationPerStageWeeks + 1) + " - " + (stageNumber * defaultDurationPerStageWeeks));
            stage.setDurationInWeeks(defaultDurationPerStageWeeks); // Đặt thời lượng cho giai đoạn hệ thống

            long targetForThisStage = (long) Math.floor(currentCigarettes / 2.0);

            long reductionPercentageValue;
            if (currentCigarettes > 0) {
                reductionPercentageValue = (long) Math.round(((double) (currentCigarettes - targetForThisStage) / currentCigarettes) * 100);
            } else {
                reductionPercentageValue = 0L;
            }
            stage.setReductionPercentage(reductionPercentageValue);
            stage.setTargetCigarettes(targetForThisStage);

            quitPlan.getStages().add(stage);
            quitPlanStageRepository.save(stage);

            currentCigarettes = targetForThisStage;
            stageNumber++;

            if (currentCigarettes <= 1 && currentCigarettes > 0) {
                QuitPlanStage finalStage = new QuitPlanStage();
                finalStage.setQuitPlan(quitPlan);
                finalStage.setStageNumber(stageNumber);
                finalStage.setWeek_range("Tuần " + ((stageNumber - 1) * defaultDurationPerStageWeeks + 1) + " - " + (stageNumber * defaultDurationPerStageWeeks));
                finalStage.setReductionPercentage(100L);
                finalStage.setTargetCigarettes(0L);
                finalStage.setDurationInWeeks(defaultDurationPerStageWeeks); // Đặt thời lượng cho giai đoạn cuối
                quitPlan.getStages().add(finalStage);
                quitPlanStageRepository.save(finalStage);
                break;
            } else if (currentCigarettes == 0) {
                break;
            }
        }
    }

    /**
     * Tính toán phần trăm giảm cho giai đoạn do người dùng tự định nghĩa.
     * @param quitPlan Kế hoạch cai thuốc.
     * @param currentStage Giai đoạn hiện tại.
     */
    private void calculateUserDefinedReductionPercentage(QuitPlan quitPlan, QuitPlanStage currentStage) {
        long previousCigarettes = 0;
        if (currentStage.getStageNumber() == 1) {
            SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(quitPlan.getAccount().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản."));
            previousCigarettes = smokingStatus.getCigarettes_per_day();
        } else {
            List<QuitPlanStage> sortedStages = quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(quitPlan.getId());
            QuitPlanStage previousStage = sortedStages.stream()
                    .filter(s -> s.getStageNumber() == currentStage.getStageNumber() - 1)
                    .findFirst()
                    .orElse(null);
            if (previousStage != null) {
                previousCigarettes = previousStage.getTargetCigarettes();
            } else {
                SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(quitPlan.getAccount().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản."));
                previousCigarettes = smokingStatus.getCigarettes_per_day();
            }
        }
        if (currentStage.getTargetCigarettes() == null) {
            throw new IllegalArgumentException("Số điếu thuốc mục tiêu không được để trống.");
        }

        if (previousCigarettes > 0) {
            long reduction = previousCigarettes - currentStage.getTargetCigarettes();
            if (reduction < 0) reduction = 0;
            long percentage = (long) Math.round(((double) reduction / previousCigarettes) * 100);
            currentStage.setReductionPercentage(percentage);
        } else {
            currentStage.setReductionPercentage(0L);
        }
    }

    /**
     * Tính tổng thời lượng của kế hoạch cai nghiện dựa trên tổng số tuần của tất cả các giai đoạn.
     * @param quitPlan Kế hoạch cai nghiện.
     * @return Tổng số tuần của kế hoạch.
     */
    private long calculateQuitPlanTotalDurationInWeeks(QuitPlan quitPlan) {
        // Lấy lại danh sách các stage từ DB để đảm bảo dữ liệu mới nhất
        List<QuitPlanStage> stages = quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(quitPlan.getId());
        return stages.stream()
                .mapToLong(QuitPlanStage::getDurationInWeeks)
                .sum();
    }
}