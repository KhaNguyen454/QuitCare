package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.PaymentMethod;
import com.BE.QuitCare.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long amountPaid; // Số tiền đã thanh toán (đơn vị: tiền đồng)

    private LocalDateTime paymentDate; // Ngày thanh toán

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // Phương thức thanh toán (VNPAY)

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // Trạng thái thanh toán (PENDING, SUCCESS, FAILED, ...)

    // --- Các trường mới để lưu thông tin từ VNPAY ---
    @Column(unique = true) // Đảm bảo tính duy nhất cho mã giao dịch VNPAY
    private String vnpTxnRef; // Mã giao dịch của hệ thống bạn, gửi sang VNPAY (vnp_TxnRef)

    @Column(length = 255) // Thông tin đơn hàng (vnp_OrderInfo)
    private String vnpOrderInfo;

    @Column(length = 50) // Mã giao dịch trên VNPAY (vnp_TransactionNo)
    private String vnpTransactionNo;

    @Column(length = 5) // Mã phản hồi từ VNPAY (00: thành công) (vnp_ResponseCode)
    private String vnpResponseCode;

    @Column(length = 20) // Mã ngân hàng (vnp_BankCode)
    private String vnpBankCode;

    @Column(length = 20) // Loại thẻ (vnp_CardType)
    private String vnpCardType;

    // --- Thời gian tạo và cập nhật ---
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Liên kết với UserMembership ---
    @ManyToOne
    @JoinColumn(name = "membership_id", nullable = false)
    private UserMembership userMembership;

//     --- Liên kết với Account (tùy chọn, nếu bạn muốn truy vấn PaymentHistory trực tiếp từ Account) ---
     @ManyToOne
     @JoinColumn(name = "account_id", nullable = false)
     private Account account;
}