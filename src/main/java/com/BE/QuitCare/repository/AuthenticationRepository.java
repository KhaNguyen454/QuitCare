package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<Account, Long> {
    //find account by  Email
    Account findAccountByEmail(String email);

    List<Account> findTop10ByRoleOrderByTotalPointDesc(Role role);


    List<Account> findByRole(Role role);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);
}
