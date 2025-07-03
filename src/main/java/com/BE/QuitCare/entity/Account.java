package com.BE.QuitCare.entity;


import com.BE.QuitCare.enums.AccountStatus;
import com.BE.QuitCare.enums.Gender;
import com.BE.QuitCare.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    @Column(nullable = false)
    private String password;

    private String username;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private LocalDateTime createAt = LocalDateTime.now();// tự sinh khi tạo

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    // Liên kết

    @OneToMany(mappedBy = "account")
    @JsonIgnore
    List<SessionUser> sessionUsers;

    @OneToMany(mappedBy = "account")
    @JsonIgnore
    private List<UserMembership> memberships;

    @OneToOne(mappedBy = "account")
    @JsonIgnore
    private SmokingStatus smokingStatus;

    @OneToOne(mappedBy = "account")
    private QuitPlan quitPlan;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PaymentHistory> paymentHistories = new ArrayList<>();


    @OneToMany(mappedBy = "account")
    @JsonIgnore
    List<Appointment> appointments;
}

