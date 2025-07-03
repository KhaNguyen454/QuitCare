package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.SmokingStatusDTO;
import com.BE.QuitCare.entity.SmokingStatus;
import com.BE.QuitCare.service.SmokingStatusService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Tình trạng hút thuốc ban đầu"
)
@RestController
@RequestMapping("/api/smoking-status")
@SecurityRequirement(name = "api")
public class SmokingStatusAPI
{
    @Autowired
    private SmokingStatusService smokingStatusService;

    // Lấy tất cả
    @GetMapping
    public ResponseEntity<List<SmokingStatusDTO>> getAll() {
        return ResponseEntity.ok(smokingStatusService.getAll());
    }

    // Lấy theo ID
    @GetMapping("/{id}")
    public ResponseEntity<SmokingStatusDTO> getById(@PathVariable Long id) {
        SmokingStatusDTO dto = smokingStatusService.getById(id);
        return (dto != null) ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<SmokingStatusDTO> getByAccountId(@PathVariable Long accountId){
        SmokingStatusDTO dto = smokingStatusService.getSmokingStatusByAccountId(accountId);
        return ResponseEntity.ok(dto);
    }

    // Tạo mới (gắn với Account)
    @PostMapping("/account/{accountId}")
    public ResponseEntity<SmokingStatusDTO> create(
            @PathVariable Long accountId,
            @RequestBody SmokingStatusDTO dto
    ) {
        try {
            SmokingStatusDTO created = smokingStatusService.create(accountId, dto);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).build(); // HTTP 409 Conflict
        }
    }


    // Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<SmokingStatusDTO> update(
            @PathVariable Long id,
            @RequestBody SmokingStatusDTO dto
    ) {
        SmokingStatusDTO updated = smokingStatusService.update(id, dto);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // Xoá
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = smokingStatusService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
