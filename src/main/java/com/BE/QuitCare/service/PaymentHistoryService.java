package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.PaymentHistoryDTO;
import com.BE.QuitCare.repository.PaymentHistoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentHistoryService {

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Lấy tất cả lịch sử thanh toán (dành cho Admin/Staff).
     * @return Danh sách PaymentHistoryDTO.
     */
    public List<PaymentHistoryDTO> getAllPaymentHistories() {
        return paymentHistoryRepository.findAll().stream()
                .map(history -> modelMapper.map(history, PaymentHistoryDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử thanh toán theo ID.
     * @param id ID của lịch sử thanh toán.
     * @return Optional chứa PaymentHistoryDTO nếu tìm thấy.
     */
    public Optional<PaymentHistoryDTO> getPaymentHistoryById(Long id) {
        return paymentHistoryRepository.findById(id)
                .map(history -> modelMapper.map(history, PaymentHistoryDTO.class));
    }

    /**
     * Lấy tất cả lịch sử thanh toán của một tài khoản cụ thể.
     * @param accountId ID của tài khoản.
     * @return Danh sách PaymentHistoryDTO.
     */
    public List<PaymentHistoryDTO> getPaymentHistoriesByAccountId(Long accountId) {
        return paymentHistoryRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(history -> modelMapper.map(history, PaymentHistoryDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả lịch sử thanh toán của một gói thành viên cụ thể.
     * @param userMembershipId ID của UserMembership.
     * @return Danh sách PaymentHistoryDTO.
     */
    public List<PaymentHistoryDTO> getPaymentHistoriesByUserMembershipId(Long userMembershipId) {
        return paymentHistoryRepository.findByUserMembershipIdOrderByCreatedAtDesc(userMembershipId).stream()
                .map(history -> modelMapper.map(history, PaymentHistoryDTO.class))
                .collect(Collectors.toList());
    }

    // Phương thức tạo và cập nhật không nên được gọi trực tiếp cho PaymentHistory
    // vì chúng được quản lý bởi luồng thanh toán trong MembershipPaymentService.
    // Tuy nhiên, nếu bạn muốn có các hàm cơ bản cho mục đích quản trị, có thể thêm vào đây.
    // Ví dụ:
    // public PaymentHistoryDTO createPaymentHistory(PaymentHistoryDTO dto) { ... }
    // public PaymentHistoryDTO updatePaymentHistory(Long id, PaymentHistoryDTO dto) { ... }

    /**
     * Xóa lịch sử thanh toán theo ID.
     * @param id ID của lịch sử thanh toán.
     * @return true nếu xóa thành công, false nếu không tìm thấy.
     */
    public boolean deletePaymentHistory(Long id) {
        if (paymentHistoryRepository.existsById(id)) {
            paymentHistoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
