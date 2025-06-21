package com.BE.QuitCare.service;


import com.BE.QuitCare.config.VNPAYConfig;
import com.BE.QuitCare.dto.PaymentHistoryDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.entity.PaymentHistory;
import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.enums.MembershipStatus;
import com.BE.QuitCare.enums.PaymentMethod;
import com.BE.QuitCare.enums.PaymentStatus;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.MembershipPlanRepository;
import com.BE.QuitCare.repository.PaymentHistoryRepository;
import com.BE.QuitCare.repository.UserMembershipRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MembershipPaymentService {

    @Autowired
    private AuthenticationRepository accountRepository;
    @Autowired
    private MembershipPlanRepository membershipPlanRepository;
    @Autowired
    private UserMembershipRepository userMembershipRepository;
    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;
    @Autowired
    private VNPAYService vnpayService;
    @Autowired
    private ModelMapper modelMapper;

    /**
     * Khởi tạo quá trình thanh toán VNPAY cho việc mua gói thành viên.
     * @param accountId ID của tài khoản người dùng.
     * @param membershipPlanId ID của gói thành viên muốn mua.
     * @param request HttpServletRequest để lấy IP và baseURL.
     * @return URL chuyển hướng đến cổng thanh toán VNPAY.
     */
    @Transactional
    public String initiateVnPayPayment(Long accountId, Long membershipPlanId, HttpServletRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + accountId));
        MembershipPlan plan = membershipPlanRepository.findById(membershipPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy gói thành viên với ID: " + membershipPlanId));

        Optional<UserMembership> existingActiveMembership = userMembershipRepository.findByAccountIdAndStatus(accountId, MembershipStatus.ACTIVE);
        if (existingActiveMembership.isPresent()) {
            throw new IllegalArgumentException("Tài khoản đã có một gói thành viên đang hoạt động.");
        }

        UserMembership userMembership = new UserMembership();
        userMembership.setAccount(account);
        userMembership.setMembershipPlan(plan);
        userMembership.setStatus(MembershipStatus.ACTIVE);
        userMembership.setStartDate(LocalDateTime.now());
        userMembership.setEndDate(LocalDateTime.now().plus(plan.getDuration()));
        userMembership.setDeleted(false);
        userMembership = userMembershipRepository.save(userMembership);

        String orderInfo = "Thanh toan goi thanh vien " + plan.getName() + " cho tai khoan " + account.getEmail();
        String vnpTxnRef = VNPAYConfig.getRandomNumber(8) + "_" + userMembership.getId();

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setAmountPaid(plan.getPrice());
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistory.setPaymentMethod(PaymentMethod.VNPAY);
        paymentHistory.setStatus(PaymentStatus.PENDING);
        paymentHistory.setUserMembership(userMembership);
        paymentHistory.setVnpTxnRef(vnpTxnRef);
        paymentHistory.setVnpOrderInfo(orderInfo);
        paymentHistory.setAccount(account); // <-- THIẾT LẬP TRƯỜNG ACCOUNT MỚI THÊM VÀO

        paymentHistory = paymentHistoryRepository.save(paymentHistory);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayReturnUrl = baseUrl + VNPAYConfig.vnp_Returnurl;

        long amountLong = (long) (plan.getPrice() * 100);

        String vnpayOrderInfo = "PM_ID_" + paymentHistory.getId();

        String vnpayUrl = vnpayService.createOrder(request, amountLong, vnpayOrderInfo, vnpayReturnUrl);

        return vnpayUrl;
    }

    /**
     * Xử lý kết quả trả về từ cổng thanh toán VNPAY.
     * @param request HttpServletRequest chứa các tham số VNPAY trả về.
     * @return PaymentHistoryDTO sau khi cập nhật trạng thái.
     */
    @Transactional
    public PaymentHistoryDTO handleVnPayReturn(HttpServletRequest request) {
        int vnpayStatusResult = vnpayService.orderReturn(request);

        String vnpTxnRef = request.getParameter("vnp_TxnRef");
        String vnpOrderInfo = request.getParameter("vnp_OrderInfo");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpBankCode = request.getParameter("vnp_BankCode");
        String vnpCardType = request.getParameter("vnp_CardType");
        String vnpPayDate = request.getParameter("vnp_PayDate");

        Long paymentHistoryId = null;
        if (vnpOrderInfo != null && vnpOrderInfo.startsWith("PM_ID_")) {
            try {
                paymentHistoryId = Long.parseLong(vnpOrderInfo.substring(6));
            } catch (NumberFormatException e) {
                System.err.println("Không thể phân tích PaymentHistory ID từ vnp_OrderInfo: " + vnpOrderInfo);
            }
        }

        PaymentHistory paymentHistory = null;
        if (paymentHistoryId != null) {
            paymentHistory = paymentHistoryRepository.findById(paymentHistoryId)
                    .orElse(null);
        }

        if (paymentHistory == null) {
            System.err.println("Không tìm thấy PaymentHistory cho vnp_OrderInfo: " + vnpOrderInfo);
            PaymentHistory dummyFailed = new PaymentHistory();
            dummyFailed.setStatus(PaymentStatus.FAILED);
            dummyFailed.setVnpOrderInfo("Invalid or missing payment record for order: " + vnpOrderInfo);
            dummyFailed.setVnpResponseCode(vnpResponseCode);
            dummyFailed.setVnpTransactionNo(vnpTransactionNo);
            // Không thể thiết lập accountId cho dummy object nếu nó không được truy xuất từ DB
            // return modelMapper.map(dummyFailed, PaymentHistoryDTO.class);
            throw new EntityNotFoundException("Payment History record not found or invalid."); // Nên ném ngoại lệ nếu đây là lỗi nghiêm trọng
        }

        // Cập nhật thông tin giao dịch VNPAY
        paymentHistory.setVnpTxnRef(vnpTxnRef);
        paymentHistory.setVnpTransactionNo(vnpTransactionNo);
        paymentHistory.setVnpResponseCode(vnpResponseCode);
        paymentHistory.setVnpBankCode(vnpBankCode);
        paymentHistory.setVnpCardType(vnpCardType);
        paymentHistory.setPaymentDate(LocalDateTime.now()); // Hoặc chuyển đổi vnpPayDate

        UserMembership userMembership = paymentHistory.getUserMembership();
        Account account = userMembership.getAccount();

        switch (vnpayStatusResult) {
            case 1: // Thanh toán thành công
                paymentHistory.setStatus(PaymentStatus.SUCCESS);
                userMembership.setStatus(MembershipStatus.ACTIVE);
                userMembership.setStartDate(LocalDateTime.now());
                userMembership.setEndDate(LocalDateTime.now().plus(userMembership.getMembershipPlan().getDuration()));
                // Cập nhật Role của người dùng thành CUSTOMER
                if (account.getRole() == Role.GUEST) {
                    account.setRole(Role.CUSTOMER);
                    accountRepository.save(account);
                    System.out.println("Cập nhật Role của tài khoản " + account.getEmail() + " thành CUSTOMER.");
                }
                break;
            case 0: // Thanh toán thất bại
                paymentHistory.setStatus(PaymentStatus.FAILED);
                userMembership.setStatus(MembershipStatus.CANCELLED);
                break;
            case -1: // Lỗi chữ ký
                paymentHistory.setStatus(PaymentStatus.INVALID_SIGNATURE);
                userMembership.setStatus(MembershipStatus.CANCELLED);
                System.err.println("Cảnh báo bảo mật: Chữ ký VNPAY không hợp lệ cho giao dịch: " + vnpTxnRef);
                break;
        }

        paymentHistoryRepository.save(paymentHistory);
        userMembershipRepository.save(userMembership);

        // Ánh xạ lại Entity đã cập nhật sang DTO, bao gồm cả accountId
        PaymentHistoryDTO resultDto = modelMapper.map(paymentHistory, PaymentHistoryDTO.class);
        resultDto.setAccountId(paymentHistory.getAccount().getId()); // Thiết lập accountId cho DTO
        return resultDto;
    }
}
