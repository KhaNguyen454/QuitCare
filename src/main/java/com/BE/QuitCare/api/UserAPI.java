package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.AccountDTO;
import com.BE.QuitCare.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/user")
public class UserAPI {
    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<AccountDTO> accounts = authenticationService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @RequestBody AccountDTO dto) {
        try {
            AccountDTO updated = authenticationService.updateAccount(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        authenticationService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

}
