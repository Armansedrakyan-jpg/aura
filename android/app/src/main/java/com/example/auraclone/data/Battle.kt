package com.example.auraclone.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Battle(
    val status: String = "",
    val createdAt: Long = 0,
    val player1: Player? = null,
    val player2: Player? = null,
    val winnerId: Long? = null
)
