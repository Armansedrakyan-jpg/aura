package com.example.auraclone.repository;

import com.example.auraclone.entity.AuraAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuraAnalysisRepository extends JpaRepository<AuraAnalysis, Long> {
    List<AuraAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AuraAnalysis> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AuraAnalysis> findTopByUserIdOrderByScoreDescCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(AVG(a.score), 0) FROM AuraAnalysis a WHERE a.userId = :userId")
    double getAverageScoreByUserId(@Param("userId") Long userId);

    @Modifying
    void deleteByUserId(Long userId);

    @Query(
            value = """
                    SELECT ranked.user_id AS userId,
                           ranked.username AS username,
                           ranked.avatar_frame AS avatarFrame,
                           ranked.avatar_effect AS avatarEffect,
                           ranked.equipped_frame_id AS equippedFrameId,
                           ranked.equipped_effect_id AS equippedEffectId,
                           ranked.score AS score,
                           ranked.level AS level,
                           ranked.verdict AS verdict,
                           ranked.created_at AS createdAt
                    FROM (
                        SELECT a.user_id,
                               u.username,
                               u.avatar_frame,
                               u.avatar_effect,
                               u.equipped_frame_id,
                               u.equipped_effect_id,
                               a.score,
                               a.level,
                               a.verdict,
                               a.created_at,
                               ROW_NUMBER() OVER (
                                   PARTITION BY a.user_id
                                   ORDER BY a.score DESC, a.created_at DESC, a.id DESC
                               ) AS rn
                        FROM aura_analysis a
                        JOIN users u ON u.id = a.user_id
                    ) ranked
                    WHERE ranked.rn = 1
                    ORDER BY ranked.score DESC, ranked.created_at DESC, ranked.user_id ASC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<LeaderboardProjection> getLeaderboard(@Param("limit") int limit);

    interface LeaderboardProjection {
        Long getUserId();
        String getUsername();
        String getAvatarFrame();
        String getAvatarEffect();
        Long getEquippedFrameId();
        Long getEquippedEffectId();
        int getScore();
        String getLevel();
        String getVerdict();
        LocalDateTime getCreatedAt();
    }
}


