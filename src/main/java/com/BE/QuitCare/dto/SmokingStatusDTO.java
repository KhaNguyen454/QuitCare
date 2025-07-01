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
     Long id;
     int started_smoking_age; // Bắt đầu hút thuốc từ năm bao nhiêu tuổi
     int cigarettes_per_day; // Số điếu hút trung bình mỗi ngày
     int cigarettes_per_pack; // Số điếu trong một gói thuốc
     TimeToFirstCigarette timeToFirstCigarette; // Thời gian hút điếu đầu sau khi thức dậy
     QuitAttempts quitAttempts; // Số lần từng cố gắng cai thuốc
     LongestQuitDuration longestQuitDuration; // Thời gian dài nhất không hút thuốc
     AccountStatus status; // Có người thân/bạn bè ủng hộ không (dựa trên mô tả của bạn)
     boolean cravingWithoutSmoking; // Bạn có cảm thấy khó chịu khi không hút thuốc
     String triggerSituation; // Bạn hút thuốc nhiều hơn khi nào
     QuitIntentionTimeline quitIntentionTimeline; // Ý định cai thuốc trong bao lâu
     ReadinessLevel readinessLevel; // Mức độ sẵn sàng cai thuốc
     QuitReasons quitReasons; // Lý do chính muốn cai thuốc
}
