package com.BE.QuitCare.api;


import com.BE.QuitCare.dto.PaymentHistoryDTO;
import com.BE.QuitCare.dto.request.PaymentInitiateRequest;
import com.BE.QuitCare.enums.PaymentStatus;
import com.BE.QuitCare.service.MembershipPlanService;
import com.BE.QuitCare.service.PaymentHistoryService;
import com.BE.QuitCare.service.VNPAYService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView; // Để redirect người dùng

import java.util.List;

@Tag(
        name = "06.Quản lý Thanh toán và Lịch sử Thanh toán"
)
@RestController
@RequestMapping("/api/v1/payments") // Base URL cho tất cả các API liên quan đến thanh toán
@SecurityRequirement(name = "api") // Yêu cầu xác thực API cho hầu hết các endpoint
public class PaymentControllerAPI {

    @Autowired
    VNPAYService vnpayService;

    @Autowired
    private PaymentHistoryService paymentHistoryService; // Service cho CRUD lịch sử thanh toán

    // --- Điểm cuối API cho việc khởi tạo và xử lý thanh toán VNPAY ---


    @PostMapping("/buy/{packageId}")
    public ResponseEntity buyPackage(@PathVariable long packageId,
                                     HttpServletRequest request
    ) {
        String clientIP = request.getHeader("X-forwarded-For");
        if(clientIP == null || clientIP.isEmpty()) {
            clientIP = request.getRemoteAddr();
        }
        String URL = vnpayService.buyMembershipPlan(packageId,clientIP);

        return ResponseEntity.ok(URL);
    }

    @PostMapping
    public ResponseEntity setStatus(@RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.ok(vnpayService.setStatus(request));
    }
    // --- Điểm cuối API cho Lịch sử Thanh toán (CRUD) ---

    /**
     * Lấy tất cả lịch sử thanh toán (thường dành cho Admin/Staff).
     * URL: GET /api/v1/payments/history
     *
     * @return Danh sách PaymentHistoryDTO.
     */
    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryDTO>> getAllPaymentHistories() {
        List<PaymentHistoryDTO> histories = paymentHistoryService.getAllPaymentHistories();
        return ResponseEntity.ok(histories);
    }

    /**
     * Lấy lịch sử thanh toán theo ID.
     * URL: GET /api/v1/payments/history/{id}
     *
     * @param id ID của lịch sử thanh toán.
     * @return PaymentHistoryDTO.
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<PaymentHistoryDTO> getPaymentHistoryById(@PathVariable Long id) {
        return paymentHistoryService.getPaymentHistoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy lịch sử thanh toán của một tài khoản cụ thể.
     * URL: GET /api/v1/payments/history/account/{accountId}
     * (Trong thực tế, accountId nên được lấy từ SecurityContext để đảm bảo người dùng chỉ xem được lịch sử của mình)
     *
     * @param accountId ID của tài khoản.
     * @return Danh sách PaymentHistoryDTO.
     */
    @GetMapping("/history/account/{accountId}")
    public ResponseEntity<List<PaymentHistoryDTO>> getPaymentHistoriesByAccountId(@PathVariable Long accountId) {
        List<PaymentHistoryDTO> histories = paymentHistoryService.getPaymentHistoriesByAccountId(accountId);
        return ResponseEntity.ok(histories);
    }

    /**
     * Lấy lịch sử thanh toán của một gói thành viên cụ thể.
     * URL: GET /api/v1/payments/history/membership/{userMembershipId}
     *
     * @param userMembershipId ID của UserMembership.
     * @return Danh sách PaymentHistoryDTO.
     */
    @GetMapping("/history/membership/{userMembershipId}")
    public ResponseEntity<List<PaymentHistoryDTO>> getPaymentHistoriesByUserMembershipId(@PathVariable Long userMembershipId) {
        List<PaymentHistoryDTO> histories = paymentHistoryService.getPaymentHistoriesByUserMembershipId(userMembershipId);
        return ResponseEntity.ok(histories);
    }

    /**
     * Xóa lịch sử thanh toán theo ID (thường giới hạn quyền cho Admin).
     * URL: DELETE /api/v1/payments/history/{id}
     *
     * @param id ID của lịch sử thanh toán.
     * @return ResponseEntity không có nội dung.
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deletePaymentHistory(@PathVariable Long id) {
        if (paymentHistoryService.deletePaymentHistory(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}