package com.example.auraclone.service;

import com.example.auraclone.dto.AuraAnalysisResult;
import com.example.auraclone.dto.LeaderboardEntryResponse;
import com.example.auraclone.entity.AuraAnalysis;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.service.AuraQuotaService.QuotaReservation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuraAnalysisService {

    private final AuraQuotaService auraQuotaService;
    private final GeminiAuraService geminiAuraService;
    private final AuraAnalysisRepository auraAnalysisRepository;
    private final AchievementService achievementService;

    public AuraAnalysisService(AuraQuotaService auraQuotaService,
                               GeminiAuraService geminiAuraService,
                               AuraAnalysisRepository auraAnalysisRepository,
                               AchievementService achievementService) {
        this.auraQuotaService = auraQuotaService;
        this.geminiAuraService = geminiAuraService;
        this.auraAnalysisRepository = auraAnalysisRepository;
        this.achievementService = achievementService;
    }

    public AuraAnalysis analyzeAndSave(Long userId, byte[] photoBytes, String detectedMime, String aboutMe) {
        QuotaReservation reservation = auraQuotaService.reserveForAnalysis(userId);
        AuraAnalysisResult result;
        try {
            result = geminiAuraService.analyze(photoBytes, detectedMime, aboutMe);
        } catch (RuntimeException ex) {
            auraQuotaService.rollbackReservation(userId, reservation);
            throw ex;
        }

        AuraAnalysis entity = new AuraAnalysis(
                userId,
                result.getScore(),
                result.getLevel(),
                result.getVibe(),
                result.getEnergy(),
                result.getConfidence(),
                result.getPersonality(),
                result.getVerdict()
        );
        AuraAnalysis saved = auraAnalysisRepository.save(entity);
        achievementService.evaluateAnalysisAchievements(userId, saved.getScore());
        return saved;
    }

    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        int safeLimit = Math.max(1, Math.min(100, limit));
        return auraAnalysisRepository.getLeaderboard(safeLimit).stream()
                .map(row -> new LeaderboardEntryResponse(
                        row.getUserId(),
                        row.getUsername(),
                        row.getAvatarFrame(),
                        row.getAvatarEffect(),
                        row.getEquippedFrameId(),
                        row.getEquippedEffectId(),
                        row.getScore(),
                        row.getLevel(),
                        row.getVerdict(),
                        row.getCreatedAt()
                ))
                .toList();
    }
}
