package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.UserMembershipDTO;
import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.exception.BadRequestException;
import com.BE.QuitCare.repository.MembershipPlanRepository;
import com.BE.QuitCare.repository.UserMembershipRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMembershipService {

    private final UserMembershipRepository repository;
    private final ModelMapper mapper;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    MembershipPlanRepository membershipPlanRepository;

    public UserMembershipService(UserMembershipRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<UserMembershipDTO> getAll() {
        return repository.findAllByDeletedFalse()
                .stream()
                .map(entity -> mapper.map(entity, UserMembershipDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<UserMembershipDTO> getById(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .map(entity -> mapper.map(entity, UserMembershipDTO.class));
    }

    public UserMembershipDTO create(UserMembershipDTO dto) {

        Account customer = authenticationService.getCurentAccount();

        MembershipPlan plan = membershipPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy gói thành viên."));

        UserMembership entity = mapper.map(dto, UserMembership.class);

        entity.setAccount(customer);
        entity.setMembershipPlan(plan);
        // Nếu không có startDate từ client, lấy thời gian hiện tại
        if (entity.getStartDate() == null) {
            entity.setStartDate(LocalDateTime.now());
        }

        // Nếu không có endDate từ client, tự động set là 30 ngày sau
        if (entity.getEndDate() == null) {
            entity.setEndDate(entity.getStartDate().plusDays(30));
        }

        entity.setDeleted(false);
        return mapper.map(repository.save(entity), UserMembershipDTO.class);
    }


    public UserMembershipDTO update(Long id, UserMembershipDTO dto) {
        Optional<UserMembership> existing = repository.findByIdAndDeletedFalse(id);
        if (existing.isPresent()) {
            UserMembership entity = existing.get();
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setStatus(dto.getStatus());
            return mapper.map(repository.save(entity), UserMembershipDTO.class);
        }
        return null;
    }

    public boolean softDelete(Long id) {
        Optional<UserMembership> entity = repository.findByIdAndDeletedFalse(id);
        if (entity.isPresent()) {
            UserMembership membership = entity.get();
            membership.setDeleted(true);
            repository.save(membership);
            return true;
        }
        return false;
    }
}
