package com.BE.QuitCare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class QuitPlanStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    int stageNumber;
    String week_range;

    @Column(nullable = false)
    Long reductionPercentage;


    @Column(nullable = false)
    Long targetCigarettes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quit_plan_id", nullable = false)
    private QuitPlan quitPlan;

    @OneToMany(mappedBy = "quitPlanStage")
    @JsonIgnore
    private List<Quitprogress>  quitProgress;

}
