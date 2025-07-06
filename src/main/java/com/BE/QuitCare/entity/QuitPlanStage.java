package com.BE.QuitCare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class QuitPlanStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    int stageNumber;

    String week_range; // Mô tả từ tuần bao nhiêu đến tuần bao nhiêu.

    @Column(nullable = false)
    Long reductionPercentage;

    @Column(nullable = false)
    Long targetCigarettes;

    @Column(nullable = false)
    int durationInWeeks;// Số lượng tuần ở trong 1 Stage

    private LocalDateTime completionDate;// ngày kết thúc của stage đó

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quit_plan_id", nullable = false)
    @JsonIgnore
    private QuitPlan quitPlan;

    @OneToMany(mappedBy = "quitPlanStage")
    @JsonIgnore
    private List<Quitprogress>  quitProgress;

}
