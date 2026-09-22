package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aura_analysis")
public class AuraAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private int score;
    private String level;
    private String vibe;
    private String energy;
    private String confidence;
    private String personality;

    @Column(length = 1000)
    private String verdict;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuraAnalysis() {
    }

    public AuraAnalysis(Long userId, int score, String level, String vibe, String energy,
                         String confidence, String personality, String verdict) {
        this.userId = userId;
        this.score = score;
        this.level = level;
        this.vibe = vibe;
        this.energy = energy;
        this.confidence = confidence;
        this.personality = personality;
        this.verdict = verdict;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getVibe() {
        return vibe;
    }

    public void setVibe(String vibe) {
        this.vibe = vibe;
    }

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
