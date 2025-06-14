package com.BE.QuitCare.api;


import com.BE.QuitCare.dto.AccountDTO;
import com.BE.QuitCare.dto.UserRequest;
import com.BE.QuitCare.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/user")
public class UserAPI
{
    @Autowired
    AuthenticationService authenticationService;

    @PutMapping("/{id}")
    public ResponseEntity<UserRequest> updateAccount(@PathVariable Long id, @RequestBody UserRequest dto) {
        try {
            UserRequest updated = authenticationService.updateForUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
