package com.example.scentguard.data.repository

import com.example.scentguard.data.model.ChartData
import com.example.scentguard.data.model.ChartPoint
import kotlinx.coroutines.delay

class ChartRepository {
    suspend fun getGasLevelHistory(): Result<ChartData> {
        delay(500) // Simulation
        val mockPoints = listOf(
            ChartPoint(0f, 180f, "10am"),
            ChartPoint(1f, 210f, "11am"),
            ChartPoint(2f, 190f, "12pm"),
            ChartPoint(3f, 250f, "1pm"),
            ChartPoint(4f, 220f, "2pm"),
            ChartPoint(5f, 185f, "3pm"),
            ChartPoint(6f, 195f, "4pm")
        )
        return Result.success(
            ChartData(
                points = mockPoints,
                minVal = 0f,
                maxVal = 500f,
                unit = "ppm"
            )
        )
    }
}
