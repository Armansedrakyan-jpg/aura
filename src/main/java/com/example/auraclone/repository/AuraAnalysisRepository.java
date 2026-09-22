package com.example.auraclone.repository;

import com.example.auraclone.entity.AuraAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuraAnalysisRepository extends JpaRepository<AuraAnalysis, Long> {
    List<AuraAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
    @org.springframework.data.jpa.repository.Query(
            "SELECT u.username AS username, a.score AS score, a.level AS level, a.verdict AS verdict " +
                    "FROM AuraAnalysis a JOIN User u ON a.userId = u.id ORDER BY a.score DESC"
    )
    List<java.util.Map<String, Object>> getLeaderboard(org.springframework.data.domain.Pageable pageable);
}
