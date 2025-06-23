package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.MessageNotificationDTO;
import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.service.MessageNotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Thông báo "
)
@RestController
@RequestMapping("/api/message-notifications")
@SecurityRequirement(name = "api")
public class MessageNotificationAPI
{
    @Autowired
    private MessageNotificationService service;

    @GetMapping
    public ResponseEntity<List<MessageNotificationDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageNotificationDTO> getById(@PathVariable Long id) {
        MessageNotificationDTO dto = service.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<MessageNotificationDTO> create(@RequestBody MessageNotificationDTO dto) {
        MessageNotificationDTO result = service.create(dto);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageNotificationDTO> update(@PathVariable Long id, @RequestBody MessageNotificationDTO dto) {
        MessageNotificationDTO result = service.update(id, dto);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> markAsDeleted(@PathVariable Long id) {
        boolean success = service.markAsDeleted(id);
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
