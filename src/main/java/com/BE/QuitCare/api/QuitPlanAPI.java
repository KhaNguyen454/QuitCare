package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.*;
import com.BE.QuitCare.service.QuitPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(
        name = "Main Flow lập kế hoạch cái thuốc "
)

@RestController
@RequestMapping("/api/v1/customers/{accountId}/quit-plans")
@SecurityRequirement(name = "api")
public class QuitPlanAPI {
    @Autowired
    private QuitPlanService quitPlanService;

    // --- Điểm cuối API cho QuitPlan ---

    /**
     * Tạo một kế hoạch cai nghiện mới cho một tài khoản cụ thể.
     * @param accountId ID của tài khoản khách hàng.
     * @param request Dữ liệu yêu cầu để tạo QuitPlan.
     * @return ResponseEntity chứa QuitPlanDTO đã được tạo và trạng thái HTTP CREATED.
     */
    @PostMapping
    public ResponseEntity<QuitPlanDTO> createQuitPlan(
            @PathVariable Long accountId,
            @RequestBody QuitPlanCreateRequest request) {
        QuitPlanDTO createdPlan = quitPlanService.createQuitPlan(accountId, request);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    /**
     * Lấy kế hoạch cai nghiện của một tài khoản cụ thể.
     * @param accountId ID của tài khoản khách hàng.
     * @return ResponseEntity chứa QuitPlanDTO của tài khoản và trạng thái HTTP OK.
     */
    @GetMapping
    public ResponseEntity<QuitPlanDTO> getQuitPlanByAccountId(@PathVariable Long accountId) {
        QuitPlanDTO quitPlan = quitPlanService.getQuitPlanByAccountId(accountId);
        return ResponseEntity.ok(quitPlan);
    }

    /**
     * Cập nhật một kế hoạch cai nghiện hiện có.
     * @param accountId ID của tài khoản khách hàng sở hữu kế hoạch.
     * @param quitPlanId ID của kế hoạch cai nghiện cần cập nhật.
     * @param request Dữ liệu yêu cầu để cập nhật QuitPlan.
     * @return ResponseEntity chứa QuitPlanDTO đã được cập nhật và trạng thái HTTP OK.
     */
    @PutMapping("/{quitPlanId}")
    public ResponseEntity<QuitPlanDTO> updateQuitPlan(
            @PathVariable Long accountId,
            @PathVariable Long quitPlanId,
            @RequestBody QuitPlanUpdateRequest request) {
        QuitPlanDTO updatedPlan = quitPlanService.updateQuitPlan(accountId, quitPlanId, request);
        return ResponseEntity.ok(updatedPlan);
    }

    /**
     * Xóa một kế hoạch cai nghiện.
     * @param accountId ID của tài khoản khách hàng sở hữu kế hoạch.
     * @param quitPlanId ID của kế hoạch cai nghiện cần xóa.
     * @return ResponseEntity không có nội dung và trạng thái HTTP NO_CONTENT.
     */
    @DeleteMapping("/{quitPlanId}")
    public ResponseEntity<Void> deleteQuitPlan(
            @PathVariable Long accountId,
            @PathVariable Long quitPlanId) {
        quitPlanService.deleteQuitPlan(accountId, quitPlanId);
        return ResponseEntity.noContent().build();
    }

    // --- Điểm cuối API cho QuitPlanStage ---

    /**
     * Tạo một giai đoạn mới trong kế hoạch cai nghiện.
     * @param accountId ID của tài khoản khách hàng sở hữu kế hoạch.
     * @param quitPlanId ID của kế hoạch cai nghiện mà giai đoạn này thuộc về.
     * @param request Dữ liệu yêu cầu để tạo QuitPlanStage.
     * @return ResponseEntity chứa QuitPlanStageDTO đã được tạo và trạng thái HTTP CREATED.
     */
    @PostMapping("/{quitPlanId}/stages")
    public ResponseEntity<QuitPlanStageDTO> createQuitPlanStage(
            @PathVariable Long accountId,
            @PathVariable Long quitPlanId,
            @RequestBody QuitPlanStageCreateRequest request) {
        // Đảm bảo quitPlanId trong request body khớp với quitPlanId trong path variable
        if (!request.getQuitPlanId().equals(quitPlanId)) {
            return ResponseEntity.badRequest().build(); // Trả về lỗi nếu không khớp
        }
        QuitPlanStageDTO createdStage = quitPlanService.createQuitPlanStage(accountId, request);
        return new ResponseEntity<>(createdStage, HttpStatus.CREATED);
    }

    /**
     * Lấy danh sách tất cả các giai đoạn của một kế hoạch cai nghiện.
     * @param accountId ID của tài khoản khách hàng sở hữu kế hoạch.
     * @param quitPlanId ID của kế hoạch cai nghiện.
     * @return ResponseEntity chứa danh sách QuitPlanStageDTO và trạng thái HTTP OK.
     */
    @GetMapping("/{quitPlanId}/stages")
    public ResponseEntity<List<QuitPlanStageDTO>> getQuitPlanStages(
            @PathVariable Long accountId,
            @PathVariable Long quitPlanId) {
        List<QuitPlanStageDTO> stages = quitPlanService.getQuitPlanStages(accountId, quitPlanId);
        return ResponseEntity.ok(stages);
    }

    /**
     * Cập nhật một giai đoạn cụ thể trong kế hoạch cai nghiện.
     * @param accountId ID của tài khoản khách hàng sở hữu kế hoạch.
     * @param quitPlanId ID của kế hoạch cai nghiện (chỉ để khớp đường dẫn, không dùng trực tiếp).
     * @param stageId ID của giai đoạn cần cập nhật.
     * @param request Dữ liệu yêu cầu để cập nhật QuitPlanStage.
     * @return ResponseEntity chứa QuitPlanStageDTO đã được cập nhật và trạng thái HTTP OK.
     */
    @PutMapping("/{quitPlanId}/stages/{stageId}")
    public ResponseEntity<QuitPlanStageDTO> updateQuitPlanStage(
            @PathVariable Long accountId,
            @PathVariable Long quitPlanId,
            @PathVariable Long stageId,
            @RequestBody QuitPlanStageUpdateRequest request) {
        QuitPlanStageDTO updatedStage = quitPlanService.updateQuitPlanStage(accountId, stageId, request);
        return ResponseEntity.ok(updatedStage);
    }

    /**
     * Xóa một giai đoạn cụ thể khỏi kế hoạch cai nghiện.
     * @param accountId ID của tài khoản khách hàng sở hữu kế hoạch.
     * @param quitPlanId ID của kế hoạch cai nghiện (chỉ để khớp đường dẫn, không dùng trực tiếp).
     * @param stageId ID của giai đoạn cần xóa.
     * @return ResponseEntity không có nội dung và trạng thái HTTP NO_CONTENT.
     */
    @DeleteMapping("/{quitPlanId}/stages/{stageId}")
    public ResponseEntity<Void> deleteQuitPlanStage(
            @PathVariable Long accountId,
            @PathVariable Long quitPlanId,
            @PathVariable Long stageId) {
        quitPlanService.deleteQuitPlanStage(accountId, stageId);
        return ResponseEntity.noContent().build();
    }
}
