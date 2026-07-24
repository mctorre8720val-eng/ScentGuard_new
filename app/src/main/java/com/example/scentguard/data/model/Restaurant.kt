package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val managerUid: String = "",
    val inviteCode: String = "",
    val createdAt: Timestamp? = null
)
