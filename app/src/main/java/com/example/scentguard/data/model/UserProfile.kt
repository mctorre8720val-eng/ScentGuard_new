package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val restaurantName: String = "",
    val restaurantId: String = "",
    val email: String = "",
    val role: String = "", // "MANAGER" or "STAFF"
    val onboardingCompleted: Boolean = false,
    val profileImageUrl: String? = null,
    val avatarType: String = "initials", // "initials", "photo", "mascot"
    val avatarId: String? = null, // "penguin", "fox", etc.
    val createdAt: Timestamp? = null
)
