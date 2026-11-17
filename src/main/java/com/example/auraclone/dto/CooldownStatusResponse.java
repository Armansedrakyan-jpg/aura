package com.example.auraclone.dto;

import java.time.LocalDateTime;

public class CooldownStatusResponse {
    public boolean canGenerateFree;
    public long remainingSeconds;
    public LocalDateTime nextFreeAt;
    public int credits;

    public CooldownStatusResponse() {
    }

    public CooldownStatusResponse(boolean canGenerateFree, long remainingSeconds, LocalDateTime nextFreeAt, int credits) {
        this.canGenerateFree = canGenerateFree;
        this.remainingSeconds = remainingSeconds;
        this.nextFreeAt = nextFreeAt;
        this.credits = credits;
    }
}
