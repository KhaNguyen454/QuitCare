package com.BE.QuitCare.service;

import com.BE.QuitCare.dto.MembershipPlanDTO;
import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.repository.MembershipPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
        // Chuyển đổi Duration sang Long (số ngày) khi gửi ra frontend
        if (entity.getDuration() != null) {
            dto.setDurationInDays(entity.getDuration().toDays());
        }
        return dto;
    }

    // Convert DTO -> Entity
    private MembershipPlan toEntity(MembershipPlanDTO dto) {
        MembershipPlan entity = new MembershipPlan();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        // Chuyển đổi Long (số ngày) từ frontend sang Duration khi lưu vào DB
        if (dto.getDurationInDays() != null) {
            entity.setDuration(Duration.ofDays(dto.getDurationInDays()));
        } else {
            entity.setDuration(Duration.ZERO); // Mặc định là 0 nếu không có duration
        }
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

    @Transactional
    public MembershipPlanDTO create(MembershipPlanDTO dto) {
        MembershipPlan plan = toEntity(dto);
        plan.setDeleted(false);
        return toDTO(repository.save(plan));
    }
    @Transactional // Thêm @Transactional
    public MembershipPlanDTO update(Long id, MembershipPlanDTO dto) {
        MembershipPlan plan = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy gói thành viên với ID: " + id)); // Ném exception rõ ràng

        plan.setName(dto.getName());
        plan.setPrice(dto.getPrice());
        plan.setDescription(dto.getDescription());
        // Chuyển đổi Long (số ngày) từ frontend sang Duration khi cập nhật
        if (dto.getDurationInDays() != null) {
            plan.setDuration(Duration.ofDays(dto.getDurationInDays()));
        } else {
            plan.setDuration(Duration.ZERO); // Mặc định là 0 nếu không có duration
        }
        // plan.setCreatedAt(LocalDateTime.now()); // createdAt chỉ nên set khi tạo
        return toDTO(repository.save(plan));
    }
    @Transactional
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
