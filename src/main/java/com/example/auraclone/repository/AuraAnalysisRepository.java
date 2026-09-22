package com.example.auraclone.repository;

import com.example.auraclone.entity.AuraAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuraAnalysisRepository extends JpaRepository<AuraAnalysis, Long> {
    List<AuraAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
}
