package com.BE.QuitCare.entity;


import com.BE.QuitCare.enums.AccountStatus;
import com.BE.QuitCare.enums.LongestQuitDuration;
import com.BE.QuitCare.enums.QuitAttempts;
import com.BE.QuitCare.enums.TimeToFirstCigarette;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Smoking_Status
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    int started_smoking_age;//Bắt đầu hút thuốc từ năm bao nhiêu tuổi
    int cigarettes_per_day;//Số điếu hút trung bình mỗi ngày
    int cigarettes_per_pack;//Số điếu trong một gói thuốc
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TimeToFirstCigarette timeToFirstCigarette;//Thời gian hút điếu đầu sau khi thức dậy

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    QuitAttempts quitAttempts;//Số lần từng cố gắng cai thuố

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    LongestQuitDuration longestQuitDuration;//Thời gian dài nhất không hút thuốc

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)

    private AccountStatus status;//Có người thân/bạn bè ủng hộ không

    private LocalDateTime createAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "account_id", unique = true)
    private Account account;


}
