package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.Quitprogress;
import com.BE.QuitCare.entity.UserAchievement;
import com.BE.QuitCare.enums.AchievementType;
import com.BE.QuitCare.repository.QuitProgressRepository;
import com.BE.QuitCare.repository.UserAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAchievementService
{
    @Autowired
    UserAchievementRepository repository;

    @Autowired
    QuitProgressRepository progressRepository;

    public void checkAndGenerate(Account account, Quitprogress latestProgress) {
        Long accountId = account.getId();

        // === 1. Không hút 1 ngày ===
        if (latestProgress.getCigarettes_smoked() == 0 &&
                !repository.existsByAccount_IdAndAchievementType(accountId, AchievementType.NO_SMOKING_ONE_DAY)) {
            create(account, latestProgress, AchievementType.NO_SMOKING_ONE_DAY, "Bạn đã không hút thuốc 1 ngày!");
        }

        // === 2. Không hút 3 ngày liên tiếp ===
        List<Quitprogress> last3 = progressRepository.findTop3BySmokingStatusOrderByDateDesc(latestProgress.getSmokingStatus());
        boolean threeDaysZero = last3.size() == 3 && last3.stream().allMatch(p -> p.getCigarettes_smoked() == 0);
        if (threeDaysZero &&
                !repository.existsByAccount_IdAndAchievementType(accountId, AchievementType.NO_SMOKING_THREE_DAYS)) {
            create(account, latestProgress, AchievementType.NO_SMOKING_THREE_DAYS, "3 ngày liên tiếp không hút thuốc!");
        }

        // === 3. Tiết kiệm ≥ 100k VNĐ ===
        int totalSaved = progressRepository.findAll().stream()
                .filter(p -> p.getSmokingStatus() != null
                        && p.getSmokingStatus().getAccount().getId().equals(accountId))
                .mapToInt(Quitprogress::getMoney_saved).sum();

        if (totalSaved >= 100_000 &&
                !repository.existsByAccount_IdAndAchievementType(accountId, AchievementType.SAVED_100K_VND)) {
            create(account, latestProgress, AchievementType.SAVED_100K_VND, "Bạn đã tiết kiệm 100.000 VNĐ!");
        }

        // === 4. Ghi nhật ký liên tục 7 ngày ===
        List<LocalDate> dates = progressRepository.findAll().stream()
                .filter(p -> p.getSmokingStatus() != null
                        && p.getSmokingStatus().getAccount().getId().equals(accountId))
                .map(Quitprogress::getDate)
                .distinct().toList();

        boolean has7Consecutive = dates.size() >= 7 &&
                dates.stream().sorted().map(LocalDate::toEpochDay)
                        .reduce((prev, curr) -> (curr - prev == 1) ? curr : null)
                        .isPresent();

        if (has7Consecutive &&
                !repository.existsByAccount_IdAndAchievementType(accountId, AchievementType.LOGGED_7_DAYS_CONSECUTIVELY)) {
            create(account, latestProgress, AchievementType.LOGGED_7_DAYS_CONSECUTIVELY, "Bạn đã ghi chú liên tục 7 ngày!");
        }
    }

    private void create(Account account, Quitprogress progress, AchievementType type, String description) {
        UserAchievement achievement = new UserAchievement();
        achievement.setAccount(account);
        achievement.setQuitprogress(progress);
        achievement.setAchievementType(type);
        achievement.setDescription(description);
        achievement.setAchievedAt(LocalDateTime.now());
        repository.save(achievement);
    }
}
