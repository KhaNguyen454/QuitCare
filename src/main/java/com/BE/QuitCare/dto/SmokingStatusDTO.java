package com.BE.QuitCare.dto;

import com.BE.QuitCare.enums.AccountStatus;
import com.BE.QuitCare.enums.LongestQuitDuration;
import com.BE.QuitCare.enums.QuitAttempts;
import com.BE.QuitCare.enums.TimeToFirstCigarette;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class SmokingStatusDTO
{
    private Long id;
    int started_smoking_age;//Bắt đầu hút thuốc từ năm bao nhiêu tuổi
    int cigarettes_per_day;//Số điếu hút trung bình mỗi ngày
    int cigarettes_per_pack;//Số điếu trong một gói thuốc
    TimeToFirstCigarette timeToFirstCigarette;//Thời gian hút điếu đầu sau khi thức dậy
    QuitAttempts quitAttempts;//Số lần từng cố gắng cai thuố
    LongestQuitDuration longestQuitDuration;//Thời gian dài nhất không hút thuốc
    private AccountStatus status;
}
