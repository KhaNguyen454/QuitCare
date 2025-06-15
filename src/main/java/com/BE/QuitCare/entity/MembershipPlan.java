package com.BE.QuitCare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class MembershipPlan
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    String name;
    double price;
    String description;
    private LocalDateTime createAt = LocalDateTime.now();

    @OneToMany(mappedBy = "membership")
    List<UserMembership>  memberships;

}
