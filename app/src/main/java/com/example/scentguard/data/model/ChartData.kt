package com.example.scentguard.data.model

data class ChartPoint(
    val x: Float,
    val y: Float,
    val label: String = ""
)

data class ChartData(
    val points: List<ChartPoint>,
    val minVal: Float,
    val maxVal: Float,
    val unit: String = "ppm"
)
