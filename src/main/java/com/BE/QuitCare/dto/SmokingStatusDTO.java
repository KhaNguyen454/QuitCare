package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.*;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmokingStatusDTO
{
    private Long id;
    private int started_smoking_age; // Bắt đầu hút thuốc từ năm bao nhiêu tuổi
    private int cigarettes_per_day; // Số điếu hút trung bình mỗi ngày
    private int cigarettes_per_pack; // Số điếu trong một gói thuốc
    private TimeToFirstCigarette timeToFirstCigarette; // Thời gian hút điếu đầu sau khi thức dậy
    private QuitAttempts quitAttempts; // Số lần từng cố gắng cai thuốc
    private LongestQuitDuration longestQuitDuration; // Thời gian dài nhất không hút thuốc
    private AccountStatus status; // Có người thân/bạn bè ủng hộ không (dựa trên mô tả của bạn)
    private boolean cravingWithoutSmoking; // Bạn có cảm thấy khó chịu khi không hút thuốc
    private String triggerSituation; // Bạn hút thuốc nhiều hơn khi nào
    private QuitIntentionTimeline quitIntentionTimeline; // Ý định cai thuốc trong bao lâu
    private ReadinessLevel readinessLevel; // Mức độ sẵn sàng cai thuốc
    private QuitReasons quitReasons; // Lý do chính muốn cai thuốc
    private LocalDateTime createAt; // Thời gian tạo bản ghi
    private Long accountId;
}
