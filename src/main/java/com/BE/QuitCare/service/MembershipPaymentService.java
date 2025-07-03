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
     * @throws EntityNotFoundException nếu không tìm thấy tài khoản hoặc gói thành viên.
     * @throws IllegalArgumentException nếu tài khoản đã có gói thành viên đang hoạt động hoặc giá gói không hợp lệ.
     * @throws RuntimeException nếu có lỗi trong quá trình tạo URL VNPAY.
     */
    @Transactional
    public String initiateVnPayPayment(Long accountId, Long membershipPlanId, HttpServletRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + accountId));
        MembershipPlan plan = membershipPlanRepository.findById(membershipPlanId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy gói thành viên với ID: " + membershipPlanId));

        // --- Bắt đầu kiểm tra giá gói thành viên ---
        if (plan.getPrice() == null) {
            throw new IllegalArgumentException("Giá gói thành viên không được để trống.");
        }
        if (plan.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá gói thành viên phải là một số dương.");
        }
        // --- Kết thúc kiểm tra giá gói thành viên ---

        Optional<UserMembership> existingActiveMembership = userMembershipRepository.findByAccountIdAndStatus(accountId, MembershipStatus.ACTIVE);
        if (existingActiveMembership.isPresent()) {
            throw new IllegalArgumentException("Tài khoản đã có một gói thành viên đang hoạt động.");
        }

        UserMembership userMembership = new UserMembership();
        userMembership.setAccount(account);
        userMembership.setMembershipPlan(plan);
        userMembership.setStatus(MembershipStatus.PENDING); // Đặt trạng thái ban đầu là PENDING
        userMembership.setStartDate(LocalDateTime.now());
        userMembership.setEndDate(LocalDateTime.now().plus(plan.getDuration()));
        userMembership.setDeleted(false);
        userMembership = userMembershipRepository.save(userMembership);

        String orderInfo = "Thanh toan goi thanh vien " + plan.getName() + " cho tai khoan " + account.getEmail();
        String vnpTxnRef = VNPAYConfig.getRandomNumber(8) + "_" + userMembership.getId(); // Gắn ID userMembership vào vnp_TxnRef

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setAmountPaid(plan.getPrice());
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistory.setPaymentMethod(PaymentMethod.VNPAY);
        paymentHistory.setStatus(PaymentStatus.PENDING);
        paymentHistory.setUserMembership(userMembership);
        paymentHistory.setVnpTxnRef(vnpTxnRef); // Lưu mã tham chiếu của hệ thống
        paymentHistory.setVnpOrderInfo(orderInfo); // Lưu thông tin đơn hàng
        paymentHistory.setAccount(account);

        paymentHistory = paymentHistoryRepository.save(paymentHistory);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayReturnUrl = baseUrl + VNPAYConfig.vnp_Returnurl;

        long amountLong = (long) (plan.getPrice().doubleValue()); // Lấy giá trị double từ Double object, sau đó ép kiểu sang long

        System.out.println("Số tiền (amountLong) gửi đến VNPAY (trước khi nhân 100 trong VNPAYService): " + amountLong);

        String vnpayOrderInfo = "PM_ID_" + paymentHistory.getId();

        String vnpayUrl;
        try {
            vnpayUrl = vnpayService.createOrder(request, amountLong, vnpayOrderInfo, vnpayReturnUrl);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi không xác định khi tạo URL thanh toán VNPAY: " + e.getMessage(), e);
        }

        return vnpayUrl;
    }

    /**
     * Xử lý kết quả trả về từ cổng thanh toán VNPAY.
     * @param request HttpServletRequest chứa các tham số VNPAY trả về.
     * @return PaymentHistoryDTO sau khi cập nhật trạng thái.
     * @throws EntityNotFoundException nếu không tìm thấy bản ghi lịch sử thanh toán hoặc không hợp lệ.
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
                // Lỗi này sẽ được MyExceptionHandler bắt nếu EntityNotFoundException được ném
            }
        }

        PaymentHistory paymentHistory = null;
        if (paymentHistoryId != null) {
            paymentHistory = paymentHistoryRepository.findById(paymentHistoryId)
                    .orElse(null);
        }

        if (paymentHistory == null) {
            throw new EntityNotFoundException("Không tìm thấy bản ghi lịch sử thanh toán hoặc không hợp lệ cho vnp_OrderInfo: " + vnpOrderInfo);
        }

        // Cập nhật thông tin giao dịch VNPAY
        paymentHistory.setVnpTxnRef(vnpTxnRef);
        paymentHistory.setVnpTransactionNo(vnpTransactionNo);
        paymentHistory.setVnpResponseCode(vnpResponseCode);
        paymentHistory.setVnpBankCode(vnpBankCode);
        paymentHistory.setVnpCardType(vnpCardType);
        paymentHistory.setPaymentDate(LocalDateTime.now());

        UserMembership userMembership = paymentHistory.getUserMembership();
        Account account = userMembership.getAccount();

        switch (vnpayStatusResult) {
            case 1: // VNPAY trả về thành công và chữ ký hợp lệ
                paymentHistory.setStatus(PaymentStatus.SUCCESS);
                userMembership.setStatus(MembershipStatus.ACTIVE); // Gói thành viên được kích hoạt
                userMembership.setStartDate(LocalDateTime.now());
                userMembership.setEndDate(LocalDateTime.now().plus(userMembership.getMembershipPlan().getDuration()));

                // Cập nhật Role của người dùng từ GUEST sang CUSTOMER
                if (account.getRole() == Role.GUEST) {
                    account.setRole(Role.CUSTOMER);
                    accountRepository.save(account);
                }
                break;
            case 0: // VNPAY trả về thất bại (mã phản hồi khác 00)
                paymentHistory.setStatus(PaymentStatus.FAILED);
                userMembership.setStatus(MembershipStatus.CANCELLED); // Gói thành viên bị hủy hoặc thất bại
                break;
            case -1: // Lỗi chữ ký (nghi ngờ giả mạo)
                paymentHistory.setStatus(PaymentStatus.INVALID_SIGNATURE);
                userMembership.setStatus(MembershipStatus.CANCELLED); // Gói thành viên bị hủy do lỗi bảo mật
                break;
        }

        paymentHistoryRepository.save(paymentHistory);
        userMembershipRepository.save(userMembership);

        PaymentHistoryDTO resultDto = modelMapper.map(paymentHistory, PaymentHistoryDTO.class);
        resultDto.setAccountId(paymentHistory.getAccount().getId());
        return resultDto;
    }
}
