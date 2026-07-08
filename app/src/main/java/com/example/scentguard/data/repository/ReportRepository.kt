package com.example.scentguard.data.repository

import com.example.scentguard.data.model.ReportSummary
import kotlinx.coroutines.delay

class ReportRepository {
    suspend fun getDailyReport(): Result<ReportSummary> {
        delay(700)
        return Result.success(
            ReportSummary(
                avgGasLevel = "142 ppm",
                totalFanRuntime = "45m",
                airQualityScore = 92,
                alertsCount = 2,
                period = "Daily"
            )
        )
    }

    suspend fun getWeeklyReport(): Result<ReportSummary> {
        delay(700)
        return Result.success(
            ReportSummary(
                avgGasLevel = "158 ppm",
                totalFanRuntime = "5h 12m",
                airQualityScore = 88,
                alertsCount = 14,
                period = "Weekly"
            )
        )
    }
}
