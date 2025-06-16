package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.UserMembershipDTO;
import com.BE.QuitCare.entity.UserMembership;
import com.BE.QuitCare.service.UserMembershipService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-memberships")
@SecurityRequirement(name = "api")
public class UserMembershipAPI {

    @Autowired
    private UserMembershipService service;

    @GetMapping
    public ResponseEntity<List<UserMembershipDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserMembershipDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserMembershipDTO> create(@RequestBody UserMembershipDTO request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserMembershipDTO> update(@PathVariable Long id, @RequestBody UserMembershipDTO request) {
        UserMembershipDTO updated = service.update(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.softDelete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
