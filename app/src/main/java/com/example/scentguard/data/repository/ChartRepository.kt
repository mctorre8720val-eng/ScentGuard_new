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
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid ID"))
        
        return try {
            val snapshot = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("sensor_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(24)
                .get()
                .await()
                
            // Reversing the list to show data points from oldest to newest on the X-axis
            val points = snapshot.documents.reversed().mapIndexed { index, doc ->
                val gas = (doc.get("currentGasPpm") as? Number)?.toFloat() ?: 0f
                val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: Date()
                val label = timeFormatter.format(timestamp)
                ChartPoint(index.toFloat(), gas, label)
            }
            
            Result.success(
                ChartData(
                    points = points,
                    minVal = 0f,
                    maxVal = 2000f,
                    unit = "ppm"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
