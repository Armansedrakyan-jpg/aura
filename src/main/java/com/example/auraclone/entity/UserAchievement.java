package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_achievement_code", columnNames = {"user_id", "code"})
        },
        indexes = {
                @Index(name = "idx_user_achievements_user", columnList = "user_id")
        }
)
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();

    public UserAchievement() {
    }

    public UserAchievement(Long userId, String code) {
        this.userId = userId;
        this.code = code;
        this.unlockedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }
}
