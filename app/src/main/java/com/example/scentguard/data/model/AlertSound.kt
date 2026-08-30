package com.example.scentguard.data.model

import androidx.annotation.RawRes
import com.example.scentguard.R

data class AlertSound(
    val id: String,
    val displayName: String,
    @param:RawRes val resId: Int
) {
    companion object {
        val CRITICAL_ALARM = AlertSound(
            id = "critical_alarm",
            displayName = "Critical Alarm",
            resId = R.raw.critical_alarm
        )

        val ALL_SOUNDS = listOf(CRITICAL_ALARM)

        fun getById(id: String?): AlertSound = ALL_SOUNDS.find { it.id == id } ?: CRITICAL_ALARM
    }
}
