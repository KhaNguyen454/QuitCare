package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.repository.MembershipPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipPlanService {

    @Autowired
    private MembershipPlanRepository repository;

    public List<MembershipPlan> getAllPlans() {
        return repository.findByDeletedFalse();
    }

    public Optional<MembershipPlan> getPlanById(Long id) {
        return repository.findById(id)
                .filter(plan -> !Boolean.TRUE.equals(plan.getDeleted()));
    }

    public MembershipPlan createPlan(MembershipPlan plan) {
        plan.setDeleted(false); // đảm bảo không bị xóa
        return repository.save(plan);
    }

    public MembershipPlan updatePlan(Long id, MembershipPlan updatedPlan) {
        return repository.findById(id).map(plan -> {
            if (Boolean.TRUE.equals(plan.getDeleted())) return null;
            plan.setName(updatedPlan.getName());
            plan.setPrice(updatedPlan.getPrice());
            plan.setDescription(updatedPlan.getDescription());
            return repository.save(plan);
        }).orElse(null);
    }

    public boolean deletePlan(Long id) {
        return repository.findById(id).map(plan -> {
            plan.setDeleted(true); // soft-delete
            repository.save(plan);
            return true;
        }).orElse(false);
    }
}
