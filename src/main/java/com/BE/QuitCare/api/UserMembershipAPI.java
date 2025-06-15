package com.BE.QuitCare.api;

import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.service.UserMembershipService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-memberships")
@SecurityRequirement(name = "api")
public class UserMembershipAPI {

    @Autowired
    private UserMembershipService service;

    @GetMapping
    public List<UserMembership> getAll() {
        return service.getAllUserMemberships();
    }

    @GetMapping("/{id}")
    public Optional<UserMembership> getById(@PathVariable Long id) {
        return service.getUserMembershipById(id);
    }

    @PostMapping
    public UserMembership create(@RequestBody UserMembership userMembership) {
        return service.createUserMembership(userMembership);
    }

    @PutMapping("/{id}")
    public UserMembership update(@PathVariable Long id, @RequestBody UserMembership updated) {
        return service.updateUserMembership(id, updated);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return service.deleteUserMembership(id);
    }
}
