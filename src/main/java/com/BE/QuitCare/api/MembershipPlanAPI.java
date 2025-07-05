package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.MembershipPlanDTO;
import com.BE.QuitCare.entity.MembershipPlan;
import com.BE.QuitCare.service.MembershipPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "05. Các gói trong hệ thống"
)
@RestController
@RequestMapping("/api/membership-plans")
@SecurityRequirement(name = "api")
public class MembershipPlanAPI {
    @Autowired
    private MembershipPlanService service;

    @GetMapping
    public ResponseEntity<List<MembershipPlanDTO>> getAllPlans() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipPlanDTO> getPlanById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MembershipPlanDTO> createPlan(@RequestBody MembershipPlanDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembershipPlanDTO> updatePlan(@PathVariable Long id, @RequestBody MembershipPlanDTO dto) {
        MembershipPlanDTO updated = service.update(id, dto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        if (service.softDelete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
