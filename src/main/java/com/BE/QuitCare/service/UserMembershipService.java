package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.UserMembershipDTO;
import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.repository.UserMembershipRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMembershipService {

    private final UserMembershipRepository repository;
    private final ModelMapper mapper;

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
        UserMembership entity = mapper.map(dto, UserMembership.class);
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
