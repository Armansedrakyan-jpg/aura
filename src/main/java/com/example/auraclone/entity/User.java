package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // хранится в виде BCrypt-хэша, никогда не plain text

    @Column(name = "last_free_analysis_at")
    private LocalDateTime lastFreeAnalysisAt;

    @Column(nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int credits = 0;

    @Column(name = "avatar_frame", nullable = false, length = 50, columnDefinition = "VARCHAR(50) NOT NULL DEFAULT 'DEFAULT'")
    private String avatarFrame = "DEFAULT";

    @Column(name = "avatar_effect", length = 50)
    private String avatarEffect;

    @Column(name = "equipped_frame_id")
    private Long equippedFrameId;

    @Column(name = "equipped_effect_id")
    private Long equippedEffectId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLastFreeAnalysisAt() {
        return lastFreeAnalysisAt;
    }

    public void setLastFreeAnalysisAt(LocalDateTime lastFreeAnalysisAt) {
        this.lastFreeAnalysisAt = lastFreeAnalysisAt;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getAvatarFrame() {
        return avatarFrame;
    }

    public void setAvatarFrame(String avatarFrame) {
        this.avatarFrame = avatarFrame;
    }

    public String getAvatarEffect() {
        return avatarEffect;
    }

    public void setAvatarEffect(String avatarEffect) {
        this.avatarEffect = avatarEffect;
    }

    public Long getEquippedFrameId() {
        return equippedFrameId;
    }

    public void setEquippedFrameId(Long equippedFrameId) {
        this.equippedFrameId = equippedFrameId;
    }

    public Long getEquippedEffectId() {
        return equippedEffectId;
    }

    public void setEquippedEffectId(Long equippedEffectId) {
        this.equippedEffectId = equippedEffectId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
