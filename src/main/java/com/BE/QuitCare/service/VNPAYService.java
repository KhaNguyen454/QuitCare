package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.request.PaymentInitiateRequest;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.entity.PaymentHistory;
import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.enums.MembershipStatus;
import com.BE.QuitCare.enums.PaymentStatus;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.NotFoundException;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.MembershipPlanRepository;
import com.BE.QuitCare.repository.PaymentHistoryRepository;
import com.BE.QuitCare.repository.UserMembershipRepository;
import com.BE.QuitCare.utils.AccountUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPAYService {
    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Autowired
    AccountUtils accountUtils;
    @Autowired
    MembershipPlanRepository membershipPlanRepository;
    @Autowired
    UserMembershipRepository userMembershipRepository;
    @Autowired
    PaymentHistoryRepository paymentRepository;
    @Autowired
    AuthenticationRepository accountRepository;

    public String createVNPayUrl(String packageId,String paymentId, long amount, String clientIp)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = LocalDateTime.now().format(formatter);
        String orderIdVnPay = UUID.randomUUID().toString().substring(0, 8);

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderIdVnPay);
        vnpParams.put("vnp_OrderInfo", "Thanh toán cho mã GD: " + packageId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // đúng định dạng: nhân 100
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl + "?packageID=" + packageId + "?paymentID=" + paymentId) ;
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_IpAddr", clientIp);

        // Build data to hash
        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

        String signData = signDataBuilder.toString();
        String signed = generateHMAC(vnp_HashSecret, signData);
        vnpParams.put("vnp_SecureHash", signed);

        // Build payment URL
        StringBuilder urlBuilder = new StringBuilder(vnp_PayUrl).append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("&");
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

        return urlBuilder.toString();
    }

    private String generateHMAC(String secretKey, String signData)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    public PaymentHistory setStatus(PaymentInitiateRequest request){
        MembershipPlan membershipPlan = membershipPlanRepository.findById(request.getMembershipPlanId())
                .orElseThrow(() -> new NotFoundException("Không thể tìm thấy gói"));
        PaymentHistory paymentHistory = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Payment!"));
        paymentHistory.setStatus(request.getPaymentStatus());
        Account currentAccount = accountUtils.getCurrentAccount();
        currentAccount.setRole(Role.CUSTOMER);
        UserMembership userMembership = new UserMembership();
        userMembership.setAccount(currentAccount);

        userMembership.setStartDate(LocalDateTime.now());
        userMembership.setEndDate(LocalDateTime.now().plus(membershipPlan.getDuration()));

        userMembershipRepository.save(userMembership);
        userMembership.setMembershipPlan(membershipPlan);
        paymentHistory.setUserMembership(userMembership);
        return paymentRepository.save(paymentHistory);
    }

    public String buyMembershipPlan(long membershipPlanId, String clientId) {
        MembershipPlan membershipPlan = membershipPlanRepository.findById(membershipPlanId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy gói!"));

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setStatus(PaymentStatus.PENDING);
        paymentHistory.setCreatedAt(LocalDateTime.now());
        paymentHistory.setAmountPaid(membershipPlan.getPrice());
        paymentRepository.save(paymentHistory);
        try {
    return createVNPayUrl(String.valueOf(membershipPlanId),String.valueOf(paymentHistory.getId()), (long) paymentHistory.getAmountPaid(), clientId);
        } catch (Exception e) {
            throw new NotFoundException("Không thể tạo URL!");
        }
    }
}
