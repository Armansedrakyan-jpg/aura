package com.example.auraclone.dto;

public class Battle {
    public String status; // WAITING, ACTIVE, FINISHED
    public long createdAt;
    public Player player1;
    public Player player2;
    public Long winnerId;

    public Battle() {

    } // Обязательный пустой конструктор для Firebase

    public Battle(String status, long createdAt, Player player1) {
        this.status = status;
        this.createdAt = createdAt;
        this.player1 = player1;
        this.player2 = null;
        this.winnerId = null;
    }
}
