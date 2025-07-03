package com.BE.QuitCare.api;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.entity.UserAchievement;
import com.BE.QuitCare.repository.AuthenticationRepository;
import com.BE.QuitCare.repository.QuitProgressRepository;
import com.BE.QuitCare.service.UserAchievementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController

public class UserAchievementAPI
{
    @Autowired
    private UserAchievementService service;

    @Autowired
    private AuthenticationRepository accountRepository;

    @Autowired
    private QuitProgressRepository quitProgressRepository;

    //  Lấy tất cả thành tựu của 1 user
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UserAchievement>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getAchievementsByUserId(userId));
    }

    //  Gọi thủ công kiểm tra achievement (nếu cần)
    @PostMapping("/generate/{userId}/{progressId}")
    public ResponseEntity<Void> checkAndGenerateAchievements(@PathVariable Long userId,
                                                             @PathVariable Long progressId) {
        Account user = accountRepository.findById(userId).orElse(null);
        Quitprogress progress = quitProgressRepository.findById(progressId).orElse(null);

        if (user == null || progress == null) {
            return ResponseEntity.notFound().build();
        }

        service.checkAndGenerate(user, progress);
        return ResponseEntity.ok().build();
    }
}
