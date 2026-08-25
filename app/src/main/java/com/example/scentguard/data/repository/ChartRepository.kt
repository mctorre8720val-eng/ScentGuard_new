package com.example.scentguard.data.repository

import com.example.scentguard.data.model.ChartData
import com.example.scentguard.data.model.ChartPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChartRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getGasLevelHistory(restaurantId: String): Result<ChartData> {
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid ID"))
        
        return try {
            val snapshot = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("sensor_history")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(24) // Last 24 records
                .get()
                .await()
                
            val points = snapshot.documents.mapIndexed { index, doc ->
                val gas = doc.getDouble("gas")?.toFloat() ?: 0f
                val label = doc.getString("timeLabel") ?: ""
                ChartPoint(index.toFloat(), gas, label)
            }
            
            Result.success(
                ChartData(
                    points = points,
                    minVal = 0f,
                    maxVal = 1000f,
                    unit = "ppm"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
