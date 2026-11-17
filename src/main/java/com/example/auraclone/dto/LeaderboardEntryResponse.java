package com.example.auraclone.dto;

import java.time.LocalDateTime;

public class LeaderboardEntryResponse {
    public Long userId;
    public String username;
    public String avatarFrame;
    public String avatarEffect;
    public Long equippedFrameId;
    public Long equippedEffectId;
    public int score;
    public String level;
    public String verdict;
    public LocalDateTime createdAt;

    public LeaderboardEntryResponse() {
    }

    public LeaderboardEntryResponse(Long userId, String username, String avatarFrame,
                                    String avatarEffect, Long equippedFrameId, Long equippedEffectId,
                                    int score, String level, String verdict, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.avatarFrame = avatarFrame;
        this.avatarEffect = avatarEffect;
        this.equippedFrameId = equippedFrameId;
        this.equippedEffectId = equippedEffectId;
        this.score = score;
        this.level = level;
        this.verdict = verdict;
        this.createdAt = createdAt;
    }
}
