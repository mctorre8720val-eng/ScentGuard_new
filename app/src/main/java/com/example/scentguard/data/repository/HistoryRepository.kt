package com.example.scentguard.data.repository

import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.model.HistoryType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HistoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getHistory(restaurantId: String): Result<List<HistoryItem>> {
        if (restaurantId.isBlank()) return Result.success(emptyList())
        
        return try {
            val snapshot = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val logs = snapshot.toObjects(HistoryItem::class.java)
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Adds a deterministic log entry to the restaurant's history.
     * Uses set() with a predefined ID to support multi-device deduplication.
     */
    suspend fun addLogEntry(restaurantId: String, log: HistoryItem): Result<Unit> {
        if (restaurantId.isBlank() || log.id.isBlank()) return Result.failure(Exception("Invalid data"))
        
        return try {
            firestore.collection("restaurants")
                .document(restaurantId)
                .collection("logs")
                .document(log.id)
                .set(log)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
