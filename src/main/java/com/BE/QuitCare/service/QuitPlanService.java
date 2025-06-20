 package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.*;
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
import java.util.List;
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

        // Kiểm tra xem tài khoản đã có QuitPlan nào chưa (đảm bảo chỉ có 1 kế hoạch tại 1 thời điểm)
        if (quitPlanRepository.findByAccountId(accountId).isPresent()) {
            throw new IllegalArgumentException("Tài khoản đã có một kế hoạch cai nghiện đang tồn tại (nháp hoặc hoạt động).");
        }

        // Lấy thông tin SmokingStatus của người dùng để tính mức độ nghiện
        SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản ID: " + accountId + ". Vui lòng điền thông tin tình trạng hút thuốc trước."));

        QuitPlan quitPlan = new QuitPlan();
        quitPlan.setAccount(account);
        quitPlan.setSystemPlan(request.isSystemPlan());
        quitPlan.setLocalDateTime(LocalDateTime.now());
        quitPlan.setQuitPlanStatus(QuitPlanStatus.DRAFT); // Trạng thái mặc định khi tạo là NHÁP

        // Tính toán AddictionLevel (Mức độ nghiện)
        quitPlan.setAddictionLevel(calculateAddictionLevel(smokingStatus));

        QuitPlan savedQuitPlan = quitPlanRepository.save(quitPlan);

        // Nếu là kế hoạch do hệ thống đề xuất, tự động tạo các giai đoạn
        if (request.isSystemPlan()) {
            generateSystemQuitPlanStages(savedQuitPlan, smokingStatus.getCigarettes_per_day());
        }

        // Ánh xạ sang DTO và trả về
        return modelMapper.map(savedQuitPlan, QuitPlanDTO.class);
    }

    public QuitPlanDTO getQuitPlanByAccountId(Long accountId) {
        QuitPlan quitPlan = quitPlanRepository.findByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện cho tài khoản ID: " + accountId));
        return modelMapper.map(quitPlan, QuitPlanDTO.class);
    }

    @Transactional
    public QuitPlanDTO updateQuitPlan(Long accountId, Long quitPlanId, QuitPlanUpdateRequest request) {
        QuitPlan quitPlan = quitPlanRepository.findById(quitPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + quitPlanId));

        // Đảm bảo kế hoạch thuộc về tài khoản đã xác thực
        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền cập nhật kế hoạch cai nghiện này.");
        }

        // Chỉ cho phép cập nhật nếu kế hoạch đang ở trạng thái NHÁP hoặc HOẠT ĐỘNG
        if (quitPlan.getQuitPlanStatus() == QuitPlanStatus.COMPLETED || quitPlan.getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể cập nhật kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        // Cập nhật trạng thái kế hoạch nếu được cung cấp
        if (request.getQuitPlanStatus() != null) {
            // Có thể thêm logic kiểm tra chuyển đổi trạng thái hợp lệ (ví dụ: DRAFT -> ACTIVE)
            quitPlan.setQuitPlanStatus(request.getQuitPlanStatus());
        }

        // Xử lý thay đổi loại kế hoạch (Hệ thống <-> Người dùng tự tạo)
        if (request.getIsSystemPlan() != null && quitPlan.isSystemPlan() != request.getIsSystemPlan()) {
            if (!quitPlan.isSystemPlan() && request.getIsSystemPlan()) { // Chuyển từ người dùng tự tạo sang hệ thống
                SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(accountId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản ID: " + accountId));
                quitPlanStageRepository.deleteAll(quitPlan.getStages()); // Xóa các giai đoạn hiện có
                quitPlan.getStages().clear(); // Rất quan trọng để xóa trong bộ nhớ
                generateSystemQuitPlanStages(quitPlan, smokingStatus.getCigarettes_per_day()); // Tạo lại các giai đoạn hệ thống
                quitPlan.setSystemPlan(true);
            } else if (quitPlan.isSystemPlan() && !request.getIsSystemPlan()) { // Chuyển từ hệ thống sang người dùng tự tạo
                quitPlanStageRepository.deleteAll(quitPlan.getStages()); // Xóa các giai đoạn hiện có
                quitPlan.getStages().clear();
                quitPlan.setSystemPlan(false);
            }
        }


        return modelMapper.map(quitPlanRepository.save(quitPlan), QuitPlanDTO.class);
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

        // Đảm bảo kế hoạch thuộc về tài khoản đã xác thực
        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền thêm giai đoạn vào kế hoạch cai nghiện này.");
        }

        // Chỉ cho phép thêm giai đoạn nếu đó là kế hoạch do người dùng tự tạo (isSystemPlan = false)
        // và kế hoạch đang ở trạng thái NHÁP hoặc HOẠT ĐỘNG
        if (quitPlan.isSystemPlan()) {
            throw new IllegalArgumentException("Không thể thêm trực tiếp giai đoạn vào kế hoạch cai nghiện do hệ thống tạo.");
        }
        if (quitPlan.getQuitPlanStatus() == QuitPlanStatus.COMPLETED || quitPlan.getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể thêm giai đoạn vào kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        QuitPlanStage stage = modelMapper.map(request, QuitPlanStage.class);
        stage.setQuitPlan(quitPlan);
        // Tính toán reductionPercentage dựa trên targetCigarettes mà người dùng nhập và giai đoạn trước/ban đầu
        calculateUserDefinedReductionPercentage(quitPlan, stage);

        return modelMapper.map(quitPlanStageRepository.save(stage), QuitPlanStageDTO.class);
    }

    public List<QuitPlanStageDTO> getQuitPlanStages(Long accountId, Long quitPlanId) {
        QuitPlan quitPlan = quitPlanRepository.findById(quitPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kế hoạch cai nghiện với ID: " + quitPlanId));

        if (!quitPlan.getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền xem các giai đoạn của kế hoạch cai nghiện này.");
        }

        return quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(quitPlanId)
                .stream()
                .map(stage -> modelMapper.map(stage, QuitPlanStageDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public QuitPlanStageDTO updateQuitPlanStage(Long accountId, Long stageId, QuitPlanStageUpdateRequest request) {
        QuitPlanStage stage = quitPlanStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giai đoạn kế hoạch cai nghiện với ID: " + stageId));

        // Đảm bảo giai đoạn thuộc về kế hoạch của tài khoản đã xác thực
        if (!stage.getQuitPlan().getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền cập nhật giai đoạn kế hoạch cai nghiện này.");
        }

        // Chỉ cho phép cập nhật các giai đoạn của kế hoạch do người dùng tự tạo (isSystemPlan = false)
        // và khi kế hoạch đang ở trạng thái NHÁP hoặc HOẠT ĐỘNG
        if (stage.getQuitPlan().isSystemPlan()) {
            throw new IllegalArgumentException("Không thể cập nhật trực tiếp các giai đoạn của kế hoạch cai nghiện do hệ thống tạo.");
        }
        if (stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.COMPLETED || stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể cập nhật các giai đoạn của kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        stage.setWeek_range(request.getWeek_range());
        stage.setTargetCigarettes(request.getTargetCigarettes());
        // Tính toán lại reductionPercentage sau khi cập nhật targetCigarettes
        calculateUserDefinedReductionPercentage(stage.getQuitPlan(), stage);

        return modelMapper.map(quitPlanStageRepository.save(stage), QuitPlanStageDTO.class);
    }

    public void deleteQuitPlanStage(Long accountId, Long stageId) {
        QuitPlanStage stage = quitPlanStageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giai đoạn kế hoạch cai nghiện với ID: " + stageId));

        if (!stage.getQuitPlan().getAccount().getId().equals(accountId)) {
            throw new SecurityException("Bạn không có quyền xóa giai đoạn kế hoạch cai nghiện này.");
        }

        // Chỉ cho phép xóa các giai đoạn của kế hoạch do người dùng tự tạo (isSystemPlan = false)
        // và khi kế hoạch đang ở trạng thái NHÁP hoặc HOẠT ĐỘNG
        if (stage.getQuitPlan().isSystemPlan()) {
            throw new IllegalArgumentException("Không thể xóa trực tiếp các giai đoạn của kế hoạch cai nghiện do hệ thống tạo.");
        }
        if (stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.COMPLETED || stage.getQuitPlan().getQuitPlanStatus() == QuitPlanStatus.CANCEL) {
            throw new IllegalArgumentException("Không thể xóa các giai đoạn của kế hoạch cai nghiện đã hoàn thành hoặc đã hủy.");
        }

        quitPlanStageRepository.delete(stage);
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
        } else { // > 30 điếu
            cigarettesPerDayScore = 3;
        }

        int totalScore = timeToFirstCigaretteScore + cigarettesPerDayScore;

        if (totalScore >= 0 && totalScore <= 2) {
            return AddictionLevel.LOW; // Nghiện thực thể nhẹ
        } else if (totalScore >= 3 && totalScore <= 4) {
            return AddictionLevel.MEDIUM; // Nghiện thực thể trung bình
        } else { // totalScore >= 5 && totalScore <= 6
            return AddictionLevel.HIGH; // Nghiện thực thể nặng
        }
    }


    private void generateSystemQuitPlanStages(QuitPlan quitPlan, int initialCigarettesPerDay) {
        long currentCigarettes = initialCigarettesPerDay;
        int stageNumber = 1;

        // Xóa tất cả các stage cũ trước khi tạo mới nếu có
        if (quitPlan.getStages() != null) {
            quitPlanStageRepository.deleteAll(quitPlan.getStages());
            quitPlan.getStages().clear();
        }

        while (currentCigarettes > 0) { // Tiếp tục tạo cho đến khi số điếu còn 0 hoặc 1
            QuitPlanStage stage = new QuitPlanStage();
            stage.setQuitPlan(quitPlan);
            stage.setStageNumber(stageNumber);
            stage.setWeek_range("Tuần " + ((stageNumber - 1) * 4 + 1) + " - " + (stageNumber * 4));

            long targetForThisStage = (long) Math.floor(currentCigarettes / 2.0);
            long reductionPercentageValue;

            if (currentCigarettes > 0) {
                reductionPercentageValue = (long) Math.round(((double) (currentCigarettes - targetForThisStage) / currentCigarettes) * 100);
            } else {
                reductionPercentageValue = 0L;
            }
            stage.setReductionPercentage(reductionPercentageValue);
            stage.setTargetCigarettes(targetForThisStage);


            quitPlan.getStages().add(stage); // Thêm vào danh sách trong Entity QuitPlan
            quitPlanStageRepository.save(stage); // Lưu giai đoạn

            currentCigarettes = targetForThisStage;
            stageNumber++;

            if (currentCigarettes <= 1 && currentCigarettes > 0) { // Nếu còn 1 điếu, tạo thêm một giai đoạn cuối cùng để về 0
                QuitPlanStage finalStage = new QuitPlanStage();
                finalStage.setQuitPlan(quitPlan);
                finalStage.setStageNumber(stageNumber);
                finalStage.setWeek_range("Tuần " + ((stageNumber - 1) * 4 + 1) + " - " + (stageNumber * 4));
                finalStage.setReductionPercentage(100L); // Giảm 100% để về 0
                finalStage.setTargetCigarettes(0L);
                quitPlan.getStages().add(finalStage);
                quitPlanStageRepository.save(finalStage);
                break; // Dừng lại sau khi tạo giai đoạn cuối
            } else if (currentCigarettes == 0) {
                break; // Dừng nếu đã về 0
            }
        }
    }

    private void calculateUserDefinedReductionPercentage(QuitPlan quitPlan, QuitPlanStage currentStage) {
        long previousCigarettes = 0;
        if (currentStage.getStageNumber() == 1) {
            // Đối với giai đoạn đầu tiên, so sánh với số điếu ban đầu từ SmokingStatus
            SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(quitPlan.getAccount().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản."));
            previousCigarettes = smokingStatus.getCigarettes_per_day();
        } else {
            // Đối với các giai đoạn tiếp theo, so sánh với targetCigarettes của giai đoạn trước đó
            // Đảm bảo các giai đoạn được sắp xếp theo stageNumber
            List<QuitPlanStage> sortedStages = quitPlanStageRepository.findByQuitPlanIdOrderByStageNumberAsc(quitPlan.getId());
            QuitPlanStage previousStage = sortedStages.stream()
                    .filter(s -> s.getStageNumber() == currentStage.getStageNumber() - 1)
                    .findFirst()
                    .orElse(null);
            if (previousStage != null) {
                previousCigarettes = previousStage.getTargetCigarettes();
            } else {
                // Nếu không tìm thấy giai đoạn trước đó, có thể có vấn đề về dữ liệu hoặc đây là giai đoạn đầu
                // Xử lý tùy theo logic nghiệp vụ của bạn (ví dụ: lấy từ SmokingStatus ban đầu)
                SmokingStatus smokingStatus = smokingStatusRepository.findByAccountId(quitPlan.getAccount().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tình trạng hút thuốc cho tài khoản."));
                previousCigarettes = smokingStatus.getCigarettes_per_day();
            }
        }

        if (previousCigarettes > 0) {
            long reduction = previousCigarettes - currentStage.getTargetCigarettes();
            if (reduction < 0) reduction = 0; // Đảm bảo không có giảm âm
            long percentage = (long) Math.round(((double) reduction / previousCigarettes) * 100);
            currentStage.setReductionPercentage(percentage);
        } else {
            currentStage.setReductionPercentage(0L); // Nếu số điếu trước đó là 0, không có giảm
        }
    }
}