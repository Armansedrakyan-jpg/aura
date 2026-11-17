package com.example.auraclone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class PublicUserProfileResponse {
    public Long userId;
    public String username;
    public LocalDateTime createdAt;
    public String avatarFrame;
    public String avatarEffect;
    public Long equippedFrameId;
    public Long equippedEffectId;
    public int bestScore;
    public String bestLevel;
    public double averageScore;
    public long totalAnalyses;
    public long totalBattles;
    public long winsCount;
    public List<AchievementDto> achievements;

    public PublicUserProfileResponse() {
    }

    public PublicUserProfileResponse(Long userId, String username, LocalDateTime createdAt,
                                     String avatarFrame, String avatarEffect,
                                     Long equippedFrameId, Long equippedEffectId,
                                     int bestScore, String bestLevel,
                                     double averageScore, long totalAnalyses, long totalBattles,
                                     long winsCount, List<AchievementDto> achievements) {
        this.userId = userId;
        this.username = username;
        this.createdAt = createdAt;
        this.avatarFrame = avatarFrame;
        this.avatarEffect = avatarEffect;
        this.equippedFrameId = equippedFrameId;
        this.equippedEffectId = equippedEffectId;
        this.bestScore = bestScore;
        this.bestLevel = bestLevel;
        this.averageScore = averageScore;
        this.totalAnalyses = totalAnalyses;
        this.totalBattles = totalBattles;
        this.winsCount = winsCount;
        this.achievements = achievements;
    }

    public static class AchievementDto {
        public String code;
        public String title;
        public String description;
        public LocalDateTime unlockedAt;

        public AchievementDto() {
        }

        public AchievementDto(String code, String title, String description, LocalDateTime unlockedAt) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.unlockedAt = unlockedAt;
        }
    }

    public static class UpdateFrameRequest {
        @NotBlank(message = "Рамка не может быть пустой")
        @Size(max = 50, message = "Название рамки слишком длинное")
        public String avatarFrame;
    }
}
