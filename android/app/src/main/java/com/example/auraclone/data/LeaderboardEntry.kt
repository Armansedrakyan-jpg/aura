package com.example.auraclone.data

data class LeaderboardEntry(
    val userId: Long = 0,
    val username: String,
    val avatarFrame: String? = "DEFAULT",
    val avatarEffect: String? = null,
    val equippedFrameId: Long? = null,
    val equippedEffectId: Long? = null,
    val score: Int,
    val level: String,
    val verdict: String,
    val createdAt: String? = null
)

