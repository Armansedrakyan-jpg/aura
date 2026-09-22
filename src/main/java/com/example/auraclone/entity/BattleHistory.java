package com.example.auraclone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "battle_history")
public class BattleHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long player1Id;
    public String player1Name;
    public int player1Score;

    public Long player2Id;
    public String player2Name;
    public int player2Score;

    public Long winnerId;
    public LocalDateTime createdAt = LocalDateTime.now();

    public BattleHistory() {}

    public BattleHistory(Long p1Id, String p1Name, int p1Score, Long p2Id, String p2Name, int p2Score, Long winnerId) {
        this.player1Id = p1Id; this.player1Name = p1Name; this.player1Score = p1Score;
        this.player2Id = p2Id; this.player2Name = p2Name; this.player2Score = p2Score;
        this.winnerId = winnerId;
    }
}
