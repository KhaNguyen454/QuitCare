package com.BE.QuitCare.api;

import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.enums.MessageStatus;
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
    public ResponseEntity<List<MessageNotification>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageNotification> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MessageNotification> create(@RequestBody MessageNotification notification) {
        return ResponseEntity.ok(service.create(notification));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageNotification> update(@PathVariable Long id, @RequestBody MessageNotification updated) {
        MessageNotification result = service.update(id, updated);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> markAsDeleted(@PathVariable Long id) {
        boolean result = service.markAsDeleted(id);
        return result ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
