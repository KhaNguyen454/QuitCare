package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.*;
import com.BE.QuitCare.dto.request.*;
import com.BE.QuitCare.dto.response.AccountResponse;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.enums.Role;
import com.BE.QuitCare.exception.AuthenticationException;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.AuthenticationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationRepository authenticationRepository;
    @Autowired
    AuthenticationManager authenticationManager; // giúp check đăng nhập
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TokenService tokenService;
  
    public Account register(RegisterRequest registerRequest){
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        if (authenticationRepository.findAccountByEmail(registerRequest.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        Account account = registerRequest.toEntity(encodedPassword);
        account.setFullName(registerRequest.getFullname()); // THÊM DÒNG NÀY

        Account savedAccount = authenticationRepository.save(account);

        try {
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setRecipient(account.getEmail());
            emailDetail.setSubject("Welcome to my system");
            emailService.sendMail(emailDetail);
        } catch (Exception e) {
            System.err.println("Không thể gửi email: " + e.getMessage());
        }

        return savedAccount;
    }


    public AccountResponse login(LoginRequest loginRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));
        }catch (Exception e){
            //Sai thông tin đăng nhập
            System.out.println("Thông tin đăng nhập không chính xác");
            throw new AuthenticationException("Invalid username or password") ;
        }

        Account account = authenticationRepository.findAccountByEmail(loginRequest.getEmail());
        AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
        String token = tokenService.generateToken(account);
        accountResponse.setToken(token);
        return accountResponse;
    }
    public List<AccountDTO> getAllAccounts() {
        return authenticationRepository.findAll()
                .stream()
                .map(account -> modelMapper.map(account, AccountDTO.class))
                .collect(Collectors.toList());
    }

    public AccountDTO updateAccount(Long id, AccountDTO dto) {
        Account account = authenticationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setFullName(dto.getFullName());
        account.setUsername(dto.getUsername());
        account.setGender(dto.getGender());
        account.setRole(dto.getRole());
        account.setStatus(dto.getStatus());

        Account updated = authenticationRepository.save(account);
        return modelMapper.map(updated, AccountDTO.class);
    }

    public UserRequest updateForUser(Long id, UserRequest dto) {
        Account account = authenticationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setFullName(dto.getFullName());
        account.setUsername(dto.getUsername());
        account.setGender(dto.getGender());

        Account updated = authenticationRepository.save(account);
        return modelMapper.map(updated, UserRequest.class);
    }
    public void deleteAccount(Long id) {
        authenticationRepository.deleteById(id);
    }


    public UpdateProfileRequest updateOwnProfile(Long id, UpdateProfileRequest dto) {
        // Lấy Account từ SecurityContext
        Account account = authenticationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setFullName(dto.getFullname());
        account.setUsername(dto.getUsername());
        account.setGender(dto.getGender());

        Account updated = authenticationRepository.save(account);
        return modelMapper.map(updated, UpdateProfileRequest.class);

    }


    public Account getCurentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Người dùng chưa xác thực.");
        }

        String username = authentication.getName(); // lấy username từ principal

        return authenticationRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tài khoản với username: " + username));
    }

    public List<CoachInfoDTO> getAllCoaches() {
        List<Account> coaches = authenticationRepository.findByRole(Role.COACH);

        return coaches.stream().map(account -> {
            CoachInfoDTO dto = new CoachInfoDTO();
            dto.setId(account.getId());
            dto.setFullName(account.getFullName());
            dto.setEmail(account.getEmail());
            dto.setGender(account.getGender() != null ? account.getGender().name() : null);
            dto.setAvatar(account.getAvatar());
            dto.setDescription(account.getDescription());
            return dto;
        }).toList();
    }


    public Account findById(Long id) {
        return authenticationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với id: " + id));
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authenticationRepository.findAccountByEmail(email);
    }
}
