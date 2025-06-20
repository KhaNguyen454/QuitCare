package com.BE.QuitCare.entity;


import com.BE.QuitCare.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class SmokingStatus
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

    @Column(nullable = false)
    boolean cravingWithoutSmoking; //Bạn có cảm thấy khó chịu khi không hút thuốc

    @Column(nullable = false)
    String triggerSituation; // Bạn hút thuốc nhiều hơn khi nào

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    QuitIntentionTimeline quitIntentionTimeline; // Ý định cai thuốc trong bao lâu

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ReadinessLevel readinessLevel;//Mức độ sẵn sàng cai thuốc

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    QuitReasons quitReasons;//Lý do chính muốn cai thuốc

    private LocalDateTime createAt = LocalDateTime.now();

    // Mối quan hệ OneToOne với Account
    // @JoinColumn định nghĩa cột khóa ngoại (foreign key) trong bảng SmokingStatus
    // unique = true đảm bảo mỗi Account chỉ có 1 SmokingStatus
    @OneToOne
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    @OneToMany(mappedBy = "smokingStatus")
    private List<Quitprogress> quitprogressList;

}
