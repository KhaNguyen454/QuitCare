package com.BE.QuitCare.api;


import com.BE.QuitCare.dto.PaymentHistoryDTO;
import com.BE.QuitCare.enums.PaymentStatus;
import com.BE.QuitCare.service.MembershipPaymentService;
import com.BE.QuitCare.service.PaymentHistoryService;
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
    private MembershipPaymentService membershipPaymentService; // Service cho logic thanh toán VNPAY

    @Autowired
    private PaymentHistoryService paymentHistoryService; // Service cho CRUD lịch sử thanh toán

    // --- Điểm cuối API cho việc khởi tạo và xử lý thanh toán VNPAY ---

    /**
     * API để khởi tạo yêu cầu thanh toán VNPAY cho gói thành viên.
     * Frontend sẽ gọi API này, nhận URL VNPAY và chuyển hướng người dùng đến đó.
     * URL: POST /api/v1/payments/initiate/{membershipPlanId}/by-account/{accountId}
     * @param accountId ID của tài khoản người dùng (trong ứng dụng thực tế, nên lấy từ SecurityContext).
     * @param membershipPlanId ID của gói thành viên muốn mua.
     * @param request HttpServletRequest để lấy thông tin IP và baseURL.
     * @return ResponseEntity chứa URL VNPAY để frontend chuyển hướng.
     */
    @PostMapping("/initiate/{membershipPlanId}/by-account/{accountId}")
    public ResponseEntity<String> initiateVnPayPayment(
            @PathVariable Long accountId,
            @PathVariable Long membershipPlanId,
            HttpServletRequest request) {
        try {
            String vnpayUrl = membershipPaymentService.initiateVnPayPayment(accountId, membershipPlanId, request);
            return ResponseEntity.ok(vnpayUrl); // Trả về URL để frontend chuyển hướng
        } catch (Exception e) {
            // Log lỗi và trả về lỗi phù hợp
            System.err.println("Lỗi khởi tạo thanh toán VNPAY: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khởi tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Endpoint này sẽ được VNPAY gọi lại sau khi người dùng hoàn tất thanh toán.
     * KHÔNG DÀNH CHO FRONTEND GỌI TRỰC TIẾP.
     * URL: GET /api/v1/payments/vnpay-return
     * (Lưu ý: Endpoint này cần được cấu hình permitAll() trong Spring Security)
     * @param request HttpServletRequest chứa các tham số trả về từ VNPAY.
     * @return RedirectView chuyển hướng người dùng về frontend với kết quả.
     */
    @GetMapping("/vnpay-return")
    public RedirectView handleVnPayReturn(HttpServletRequest request) {
        PaymentHistoryDTO paymentResult;
        // Thay thế bằng URL frontend của bạn (Ví dụ: http://localhost:3000/payment-success)
        String frontendSuccessUrl = "http://http://localhost:8080/payment-success";
        String frontendFailUrl = "http://localhost:8080/payment-fail";

        try {
            paymentResult = membershipPaymentService.handleVnPayReturn(request);

            if (paymentResult.getStatus() == PaymentStatus.SUCCESS) {
                // Chuyển hướng về trang thành công của frontend, có thể kèm theo transaction ID
                return new RedirectView(frontendSuccessUrl + "?transactionId=" + paymentResult.getVnpTransactionNo() + "&status=success");
            } else {
                // Chuyển hướng về trang thất bại của frontend, có thể kèm theo mã lỗi
                return new RedirectView(frontendFailUrl + "?status=" + paymentResult.getStatus().name() + "&code=" + paymentResult.getVnpResponseCode());
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ trong quá trình xử lý VNPAY return
            System.err.println("Lỗi xử lý VNPAY return: " + e.getMessage());
            return new RedirectView(frontendFailUrl + "?error=" + e.getMessage());
        }
    }

    // --- Điểm cuối API cho Lịch sử Thanh toán (CRUD) ---

    /**
     * Lấy tất cả lịch sử thanh toán (thường dành cho Admin/Staff).
     * URL: GET /api/v1/payments/history
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