package com.example.scentguard.data.repository

import com.example.scentguard.data.model.NotificationItem
import com.example.scentguard.data.model.NotificationType
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.*

class NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationItem>> {
        delay(600)
        return try {
            val mockData = listOf(
                NotificationItem(
                    id = "1",
                    title = "Critical Gas Level",
                    message = "High concentration detected in storage room A. Ventilation active.",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 1200000)),
                    type = NotificationType.ALERT,
                    isRead = false
                ),
                NotificationItem(
                    id = "2",
                    title = "System Update",
                    message = "ScentGuard firmware updated to version 1.2.0.",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000)),
                    type = NotificationType.SYSTEM,
                    isRead = true
                ),
                NotificationItem(
                    id = "3",
                    title = "Maintenance Reminder",
                    message = "Monthly sensor calibration recommended.",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 172800000)),
                    type = NotificationType.WARNING,
                    isRead = true
                )
            )
            Result.success(mockData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
