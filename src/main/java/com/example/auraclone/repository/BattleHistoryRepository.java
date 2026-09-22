package com.example.auraclone.repository;

import com.example.auraclone.entity.BattleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleHistoryRepository extends JpaRepository<BattleHistory, Long> {
}
