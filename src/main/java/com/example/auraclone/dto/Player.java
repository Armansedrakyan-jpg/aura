package com.example.auraclone.dto;

public class Player {
    public Long userId;
    public String username;
    public int score;
    public String title;

    public Player() {

    } // Обязательный пустой конструктор для Firebase

    public Player(Long userId, String username, int score, String title) {
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.title = title;
    }
}
