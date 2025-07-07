package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.AddictionLevel;
import com.BE.QuitCare.enums.QuitPlanStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class QuitPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    AddictionLevel addictionLevel;

    boolean isSystemPlan;

    LocalDateTime localDateTime;

    @Enumerated(EnumType.STRING)
    private QuitPlanStatus quitPlanStatus;

    // Mối quan hệ OneToOne với Account (chỉ cho Role CUSTOMER)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    @JsonIgnore
    private Account account;

    // Mối quan hệ OneToMany với QuitPlanStage
    // mappedBy chỉ ra trường "quitPlan" trong QuitPlanStage là chủ sở hữu mối quan hệ
    // cascade = CascadeType.ALL để các thao tác trên QuitPlan ảnh hưởng đến các Stage liên quan
    // orphanRemoval = true để xóa các Stage khi chúng bị gỡ bỏ khỏi danh sách của QuitPlan
    @OneToMany(mappedBy = "quitPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stageNumber ASC") // Sắp xếp các giai đoạn theo số thứ tự
    private List<QuitPlanStage> stages = new ArrayList<>();
}
