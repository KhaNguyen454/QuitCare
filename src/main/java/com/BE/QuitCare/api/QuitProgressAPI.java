package com.BE.QuitCare.api;

import com.BE.QuitCare.dto.QuitProgressDTO;
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
        name = "Main Flow theo dõi tiến trình cai thuốc"
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
    public ResponseEntity<Quitprogress> update(@PathVariable Long id, @RequestBody Quitprogress updated) {
        Quitprogress result = service.update(id, updated);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
    @GetMapping("/check-missed")
    public ResponseEntity<?> checkMissed(
            @RequestParam Long smokingStatusId,
            @RequestParam String date // ISO: yyyy-MM-dd
    ) {
        LocalDate localDate = LocalDate.parse(date);
        Quitprogress result = service.checkAndMarkMissed(smokingStatusId, localDate);

        if (result == null) {
            return ResponseEntity.ok("Đã có bản ghi trong ngày, không cần đánh dấu missed.");
        }
        return ResponseEntity.ok("Đã đánh dấu missed cho ngày " + date);
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


}
