package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthenticationService implements UserDetailsService {


    @Autowired
    private AuthenticationRepository authenticationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    public Account register(Account account){
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account newAccount = authenticationRepository.save(account);

        return newAccount;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authenticationRepository.findAccountByEmail(email);
    }
}
