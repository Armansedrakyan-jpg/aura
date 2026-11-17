package com.example.auraclone.service;

import com.example.auraclone.dto.PublicUserProfileResponse.AchievementDto;
import com.example.auraclone.entity.UserAchievement;
import com.example.auraclone.repository.AuraAnalysisRepository;
import com.example.auraclone.repository.BattleHistoryRepository;
import com.example.auraclone.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AchievementService {

    public record AchievementDefinition(String code, String title, String description) {}

    private static final Map<String, AchievementDefinition> DEFINITIONS = Map.of(
            "FIRST_ANALYSIS", new AchievementDefinition("FIRST_ANALYSIS", "Первый скан", "Пройден первый анализ ауры"),
            "FIVE_ANALYSES", new AchievementDefinition("FIVE_ANALYSES", "Исследователь ауры", "Выполнено 5 и более анализов ауры"),
            "SCORE_80_PLUS", new AchievementDefinition("SCORE_80_PLUS", "Сильная аура", "Достигнут результат 80+ баллов"),
            "SCORE_95_PLUS", new AchievementDefinition("SCORE_95_PLUS", "Легенда", "Достигнут результат 95+ баллов"),
            "FIRST_BATTLE_WIN", new AchievementDefinition("FIRST_BATTLE_WIN", "Первая победа", "Выиграна первая дуэль"),
            "FIVE_BATTLE_WINS", new AchievementDefinition("FIVE_BATTLE_WINS", "Чемпион арены", "Одержано 5 и более побед в дуэлях")
    );

    private final UserAchievementRepository userAchievementRepository;
    private final AuraAnalysisRepository auraAnalysisRepository;
    private final BattleHistoryRepository battleHistoryRepository;

    public AchievementService(UserAchievementRepository userAchievementRepository,
                              AuraAnalysisRepository auraAnalysisRepository,
                              BattleHistoryRepository battleHistoryRepository) {
        this.userAchievementRepository = userAchievementRepository;
        this.auraAnalysisRepository = auraAnalysisRepository;
        this.battleHistoryRepository = battleHistoryRepository;
    }

    @Transactional
    public void evaluateAnalysisAchievements(Long userId, int score) {
        long totalAnalyses = auraAnalysisRepository.countByUserId(userId);
        if (totalAnalyses >= 1) {
            grantIfMissing(userId, "FIRST_ANALYSIS");
        }
        if (totalAnalyses >= 5) {
            grantIfMissing(userId, "FIVE_ANALYSES");
        }
        if (score >= 80) {
            grantIfMissing(userId, "SCORE_80_PLUS");
        }
        if (score >= 95) {
            grantIfMissing(userId, "SCORE_95_PLUS");
        }
    }

    @Transactional
    public void evaluateBattleAchievements(Long winnerId) {
        if (winnerId == null || winnerId <= 0L) {
            return;
        }
        long wins = battleHistoryRepository.countByWinnerId(winnerId);
        if (wins >= 1) {
            grantIfMissing(winnerId, "FIRST_BATTLE_WIN");
        }
        if (wins >= 5) {
            grantIfMissing(winnerId, "FIVE_BATTLE_WINS");
        }
    }

    public List<AchievementDto> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserIdOrderByUnlockedAtAsc(userId).stream()
                .map(ua -> {
                    AchievementDefinition def = DEFINITIONS.get(ua.getCode());
                    String title = def != null ? def.title() : ua.getCode();
                    String description = def != null ? def.description() : "";
                    return new AchievementDto(ua.getCode(), title, description, ua.getUnlockedAt());
                })
                .toList();
    }

    private void grantIfMissing(Long userId, String code) {
        if (!userAchievementRepository.existsByUserIdAndCode(userId, code)) {
            userAchievementRepository.save(new UserAchievement(userId, code));
        }
    }
}
