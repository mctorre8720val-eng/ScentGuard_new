package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val managerUid: String = "",
    val inviteCode: String = "",
    val inviteCodeExpiresAt: Timestamp? = null,
    val currentGasPpm: Int = 0,
    val airStatus: String = "SAFE", // "SAFE", "WARN", "DANGER"
    val fanStatus: String = "OFF", // "ON", "OFF"
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val lastSeen: Timestamp? = null,
    val fanMode: String = "AUTO", // "ON", "OFF", "AUTO"
    val createdAt: Timestamp? = null
)
