package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticationRepository extends JpaRepository<Account, Long> {
    //find account by  Email
    Account findAccountByEmail(String email);

    List<Account> findTop10ByOrderByTotalPointDesc();

    List<Account> findByRole(Role role);

}
