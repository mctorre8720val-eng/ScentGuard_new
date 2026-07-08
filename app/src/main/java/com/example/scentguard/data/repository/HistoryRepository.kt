package com.example.scentguard.data.repository

import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.model.HistoryType
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.*

class HistoryRepository {
    suspend fun getHistory(): Result<List<HistoryItem>> {
        // Simulating network delay
        delay(800)
        return try {
            val mockData = listOf(
                HistoryItem(
                    id = "1",
                    title = "Fan Activated",
                    description = "Automatic safety trigger - High gas level",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 3600000)),
                    type = HistoryType.WARNING,
                    value = "250 ppm"
                ),
                HistoryItem(
                    id = "2",
                    title = "Air Quality Normal",
                    description = "Gas levels stabilized",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 3300000)),
                    type = HistoryType.SUCCESS,
                    value = "120 ppm"
                ),
                HistoryItem(
                    id = "3",
                    title = "High Gas Detected",
                    description = "Alert: Threshold exceeded",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 7200000)),
                    type = HistoryType.ALERT,
                    value = "410 ppm"
                ),
                HistoryItem(
                    id = "4",
                    title = "System Periodic Check",
                    description = "All sensors operating normally",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 14400000)),
                    type = HistoryType.INFO
                ),
                HistoryItem(
                    id = "5",
                    title = "Manual Fan Override",
                    description = "Manager initiated ventilation",
                    timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000)),
                    type = HistoryType.INFO
                )
            )
            Result.success(mockData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
