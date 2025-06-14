package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.UpdateProfileRequest;
import com.BE.QuitCare.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name ="api")
@RequestMapping("/api/user")
public class UserAPI {
    @Autowired
    AuthenticationService authenticationService;

    @PutMapping("/{id}")
    public ResponseEntity<UpdateProfileRequest> updateAccount(@PathVariable Long id, @RequestBody UpdateProfileRequest dto) {
        try {
            UpdateProfileRequest updated = authenticationService.updateOwnProfile(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
