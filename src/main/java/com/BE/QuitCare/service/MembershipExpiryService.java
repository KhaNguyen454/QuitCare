package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.enums.MembershipStatus;
import com.BE.QuitCare.repository.UserMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MembershipExpiryService {

    @Autowired
    private UserMembershipRepository userMembershipRepository;

    /**
     * Tác vụ định kỳ để kiểm tra và cập nhật trạng thái gói thành viên đã hết hạn.
     * Chạy mỗi ngày một lần vào lúc 00:00 (nửa đêm).
     * cron = "0 0 0 * * ?" nghĩa là:
     * giây (0) phút (0) giờ (0) ngày trong tháng (*) tháng (*) ngày trong tuần (?)
     */
    @Scheduled(cron = "${app.membership.expiry.cron-expression}") // Cấu hình cron expression trong application.properties
    @Transactional // Đảm bảo toàn bộ quá trình cập nhật diễn ra trong một transaction
    public void updateExpiredMemberships() {
        System.out.println("Bắt đầu kiểm tra các gói thành viên đã hết hạn vào lúc: " + LocalDateTime.now());

        // Tìm tất cả các gói thành viên đang ACTIVE và có endDate trong quá khứ hoặc hiện tại
        List<UserMembership> expiredMemberships = userMembershipRepository.findByStatusAndEndDateBefore(
                MembershipStatus.ACTIVE,
                LocalDateTime.now()
        );

        if (expiredMemberships.isEmpty()) {
            System.out.println("Không tìm thấy gói thành viên nào đã hết hạn cần cập nhật.");
            return;
        }

        System.out.println("Tìm thấy " + expiredMemberships.size() + " gói thành viên đã hết hạn. Đang cập nhật trạng thái...");

        for (UserMembership membership : expiredMemberships) {
            membership.setStatus(MembershipStatus.EXPIRED); // Chuyển trạng thái sang EXPIRED
            userMembershipRepository.save(membership); // Lưu thay đổi vào DB
            System.out.println("Đã cập nhật gói thành viên ID: " + membership.getId() + " sang trạng thái INACTIVE.");
        }

        System.out.println("Hoàn tất kiểm tra và cập nhật gói thành viên đã hết hạn.");
    }
}
