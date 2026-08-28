package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class HistoryItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: HistoryType = HistoryType.INFO,
    val value: String? = null,
    // Phase 5B: Extended fields for automated logging
    val eventType: String = "",
    val gasPpm: Int? = null,
    val fanStatus: String? = null,
    val fanMode: String? = null,
    val airStatus: String? = null,
    val source: String? = null // "MANUAL", "AUTOMATIC", "SYSTEM"
)

enum class HistoryType {
    INFO, WARNING, ALERT, SUCCESS
}
