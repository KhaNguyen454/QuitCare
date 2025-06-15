package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.repository.UserMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserMembershipService {

    @Autowired
    private UserMembershipRepository repository;

    public List<UserMembership> getAllUserMemberships() {
        return repository.findByDeletedFalse();
    }

    public Optional<UserMembership> getUserMembershipById(Long id) {
        return repository.findById(id);
    }

    public UserMembership createUserMembership(UserMembership userMembership) {
        return repository.save(userMembership);
    }

    public UserMembership updateUserMembership(Long id, UserMembership updated) {
        return repository.findById(id).map(um -> {
            um.setStartDate(updated.getStartDate());
            um.setEndDate(updated.getEndDate());
            um.setStatus(updated.getStatus());
            um.setAccount(updated.getAccount());
            um.setMembershipPlan(updated.getMembershipPlan());
            return repository.save(um);
        }).orElse(null);
    }

    public boolean deleteUserMembership(Long id) {
        Optional<UserMembership> optional = repository.findById(id);
        if (optional.isPresent()) {
            UserMembership um = optional.get();
            um.setDeleted(true);
            repository.save(um);
            return true;
        }
        return false;
    }

}
