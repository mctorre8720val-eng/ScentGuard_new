package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.ALERT
)

enum class NotificationType {
    ALERT, WARNING, SYSTEM
}
