package com.example.scentguard.data.repository

import com.example.scentguard.data.model.ChartData
import com.example.scentguard.data.model.ChartPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ChartRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    suspend fun getGasLevelHistory(restaurantId: String): Result<ChartData> {
        return getSensorHistory(restaurantId, "currentGasPpm", 0f, 2000f, "ppm")
    }

    suspend fun getTemperatureHistory(restaurantId: String): Result<ChartData> {
        return getSensorHistory(restaurantId, "temperature", 0f, 100f, "°C")
    }

    private suspend fun getSensorHistory(
        restaurantId: String,
        fieldName: String,
        minVal: Float,
        maxVal: Float,
        unit: String
    ): Result<ChartData> {
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid ID"))
        
        return try {
            val snapshot = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("sensor_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(24)
                .get()
                .await()
                
            val points = snapshot.documents.reversed().mapIndexed { index, doc ->
                val value = (doc.get(fieldName) as? Number)?.toFloat() ?: 0f
                val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date()
                val label = timeFormatter.format(timestamp)
                ChartPoint(index.toFloat(), value, label)
            }
            
            Result.success(
                ChartData(
                    points = points,
                    minVal = minVal,
                    maxVal = maxVal,
                    unit = unit
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
