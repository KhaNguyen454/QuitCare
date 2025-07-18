package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.QuitProgressDTO;
import com.BE.QuitCare.dto.QuitProgressDTO2;
import com.BE.QuitCare.entity.MessageNotification;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.repository.MessageNotificationRepository;
import com.BE.QuitCare.repository.QuitProgressRepository;
import com.BE.QuitCare.service.QuitProgressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Tag(
        name = "10. Main Flow theo dõi tiến trình cai thuốc"
)

@RestController
@RequestMapping("/api/quit-progress")
@SecurityRequirement(name = "api")
public class  QuitProgressAPI
{
    @Autowired
    private QuitProgressService service;
    @Autowired
    private QuitProgressRepository quitProgressRepository;
    @Autowired
    private MessageNotificationRepository messageNotificationRepository;

    @GetMapping
    public ResponseEntity<List<Quitprogress>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quitprogress> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Quitprogress> create(@RequestBody QuitProgressDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Quitprogress> update(@PathVariable Long id, @RequestBody QuitProgressDTO dto) {
        Quitprogress result = service.update(id, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-auto-missed")
    public ResponseEntity<?> autoCheckMissed(@RequestParam Long smokingStatusId) {
        List<Quitprogress> missedList = service.autoCheckMissedDays(smokingStatusId);
        return ResponseEntity.ok("Đã tạo " + missedList.size() + " bản ghi missed.");
    }



    @PostMapping("/generate-notification/{progressId}")
    public ResponseEntity<List<MessageNotification>> generateNotification(@PathVariable Long progressId) {
        Optional<Quitprogress> progressOpt = quitProgressRepository.findById(progressId);
        if (progressOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<MessageNotification> notifications = service.generateNotifications(progressOpt.get());
        List<MessageNotification> saved = messageNotificationRepository.saveAll(notifications);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/stage/{stageId}/user/{userId}")
    public ResponseEntity<QuitProgressDTO2> getProgressByStage(
            @PathVariable Long stageId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(service.getProgressByStage(userId, stageId));
    }

}