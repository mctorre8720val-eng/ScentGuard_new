package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val fullName: String = "",
    val restaurantName: String = "",
    val email: String = "",
    val role: String = "",
    val createdAt: Timestamp? = null
)
