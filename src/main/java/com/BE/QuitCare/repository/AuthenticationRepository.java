package com.BE.QuitCare.repository;

import com.BE.QuitCare.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationRepository extends JpaRepository<Account, Long> {
    //find account by  Email
    Account findAccountByEmail(String email);

}
