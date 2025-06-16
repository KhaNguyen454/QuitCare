package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.AccountDTO;
import com.BE.QuitCare.repository.CommunityPostRepository;
import com.BE.QuitCare.service.AuthenticationService;
import com.BE.QuitCare.service.CommentService;
import com.BE.QuitCare.service.CommunityPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/admin")
public class AdminAPI {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private CommunityPostService communityPostService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/user")
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<AccountDTO> accounts = authenticationService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @RequestBody AccountDTO dto) {
        try {
            AccountDTO updated = authenticationService.updateAccount(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        authenticationService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

}
