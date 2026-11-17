package com.example.auraclone.repository;

import com.example.auraclone.entity.BattleHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BattleHistoryRepository extends JpaRepository<BattleHistory, Long> {

    @Query("SELECT b FROM BattleHistory b WHERE b.player1Id = :userId OR b.player2Id = :userId ORDER BY b.createdAt DESC")
    List<BattleHistory> findUserBattles(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BattleHistory b WHERE b.player1Id = :userId OR b.player2Id = :userId")
    long countUserBattles(@Param("userId") Long userId);

    long countByWinnerId(Long winnerId);

    @Modifying
    @Query("UPDATE BattleHistory b SET b.player1Name = 'Удалённый игрок' WHERE b.player1Id = :userId")
    void anonymizePlayer1(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE BattleHistory b SET b.player2Name = 'Удалённый игрок' WHERE b.player2Id = :userId")
    void anonymizePlayer2(@Param("userId") Long userId);
}

