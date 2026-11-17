package com.example.auraclone.service;

import com.example.auraclone.dto.PublicUserProfileResponse;
import com.example.auraclone.dto.PublicUserProfileResponse.AchievementDto;
import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.entity.User;
import com.example.auraclone.exception.ApiException;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.repository.BattleHistoryRepository;
import com.example.auraclone.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserProfileService {

    public static final Set<String> ALLOWED_FRAMES = Set.of(
            "DEFAULT", "NEON", "GOLD", "CYBER", "SIGMA", "LEGENDARY"
    );

    private final UserRepository userRepository;
    private final AuraAnalysisRepository auraAnalysisRepository;
    private final BattleHistoryRepository battleHistoryRepository;
    private final AchievementService achievementService;

    public UserProfileService(UserRepository userRepository,
                              AuraAnalysisRepository auraAnalysisRepository,
                              BattleHistoryRepository battleHistoryRepository,
                              AchievementService achievementService) {
        this.userRepository = userRepository;
        this.auraAnalysisRepository = auraAnalysisRepository;
        this.battleHistoryRepository = battleHistoryRepository;
        this.achievementService = achievementService;
    }

    public PublicUserProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        AuraAnalysis bestAnalysis = auraAnalysisRepository
                .findTopByUserIdOrderByScoreDescCreatedAtDesc(userId)
                .orElse(null);

        long totalAnalyses = auraAnalysisRepository.countByUserId(userId);
        double avgRaw = totalAnalyses > 0 ? auraAnalysisRepository.getAverageScoreByUserId(userId) : 0.0;
        double averageScore = Math.round(avgRaw * 10.0) / 10.0;

        long totalBattles = battleHistoryRepository.countUserBattles(userId);
        long winsCount = battleHistoryRepository.countByWinnerId(userId);
        List<AchievementDto> achievements = achievementService.getUserAchievements(userId);

        return new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getAvatarFrame(),
                user.getAvatarEffect(),
                user.getEquippedFrameId(),
                user.getEquippedEffectId(),
                bestAnalysis != null ? bestAnalysis.getScore() : 0,
                bestAnalysis != null ? bestAnalysis.getLevel() : null,
                averageScore,
                totalAnalyses,
                totalBattles,
                winsCount,
                achievements
        );
    }

    @Transactional
    public String updateAvatarFrame(Long userId, String requestedFrame) {
        String normalized = requestedFrame == null ? "" : requestedFrame.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_FRAMES.contains(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Недопустимая рамка аватара");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        user.setAvatarFrame(normalized);
        userRepository.save(user);
        return normalized;
    }
}
