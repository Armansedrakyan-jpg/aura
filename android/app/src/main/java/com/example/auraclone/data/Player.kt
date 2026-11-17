package com.example.auraclone.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Player(
    val userId: Long = 0,
    val username: String = "",
    val score: Int = 0,
    val title: String = ""
)
