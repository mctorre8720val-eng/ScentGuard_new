package com.example.scentguard.data.model

import com.google.firebase.Timestamp

data class HistoryItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: HistoryType = HistoryType.INFO,
    val value: String? = null
)

enum class HistoryType {
    INFO, WARNING, ALERT, SUCCESS
}
