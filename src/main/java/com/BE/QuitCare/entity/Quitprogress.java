package com.BE.QuitCare.entity;

import com.BE.QuitCare.enums.QuitHealthStatus;
import com.BE.QuitCare.enums.QuitProgressStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
public class Quitprogress
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    LocalDate date;
    int cigarettes_smoked;//số điếu hút

    @Enumerated(EnumType.STRING)
    QuitHealthStatus quitHealthStatus;
    int money_saved;
    @Enumerated(EnumType.STRING)
    QuitProgressStatus quitProgressStatus;
    int point;

    @ManyToOne
      @JoinColumn(name = "quitPlanStage_id")
    private QuitPlanStage quitPlanStage;

    @OneToMany(mappedBy = "quitprogress")
    private List<MessageNotification>  messageNotifications;

    @ManyToOne
    @JoinColumn(name = "smoking_status_id")
    private SmokingStatus smokingStatus;
}
