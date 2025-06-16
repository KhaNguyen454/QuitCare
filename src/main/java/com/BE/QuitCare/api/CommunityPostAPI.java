package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.CommunityPostDTO;
import com.BE.QuitCare.entity.CommunityPost;
import com.BE.QuitCare.service.CommunityPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community-posts")
@SecurityRequirement(name = "api")
public class CommunityPostAPI {


    @Autowired
    private CommunityPostService service;

    @GetMapping
    public ResponseEntity<List<CommunityPostDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityPostDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CommunityPostDTO> create(@RequestBody CommunityPostDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommunityPostDTO> update(@PathVariable Long id, @RequestBody CommunityPostDTO dto) {
        CommunityPostDTO updated = service.update(id, dto);
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
