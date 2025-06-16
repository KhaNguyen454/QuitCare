package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.MembershipPlanDTO;
import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.repository.MembershipPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MembershipPlanService {

    @Autowired
    private MembershipPlanRepository repository;

    // Convert Entity -> DTO
    private MembershipPlanDTO toDTO(MembershipPlan entity) {
        MembershipPlanDTO dto = new MembershipPlanDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    // Convert DTO -> Entity
    private MembershipPlan toEntity(MembershipPlanDTO dto) {
        MembershipPlan entity = new MembershipPlan();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public List<MembershipPlanDTO> getAll() {
        return repository.findByDeletedFalse()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<MembershipPlanDTO> getById(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .map(this::toDTO);
    }

    public MembershipPlanDTO create(MembershipPlanDTO dto) {
        MembershipPlan plan = toEntity(dto);
        plan.setDeleted(false);
        return toDTO(repository.save(plan));
    }

    public MembershipPlanDTO update(Long id, MembershipPlanDTO dto) {
        Optional<MembershipPlan> optionalPlan = repository.findByIdAndDeletedFalse(id);
        if (optionalPlan.isPresent()) {
            MembershipPlan plan = optionalPlan.get();
            plan.setName(dto.getName());
            plan.setPrice(dto.getPrice());
            plan.setDescription(dto.getDescription());
            return toDTO(repository.save(plan));
        }
        return null;
    }

    public boolean softDelete(Long id) {
        Optional<MembershipPlan> optionalPlan = repository.findByIdAndDeletedFalse(id);
        if (optionalPlan.isPresent()) {
            MembershipPlan plan = optionalPlan.get();
            plan.setDeleted(true);
            repository.save(plan);
            return true;
        }
        return false;
    }
}
