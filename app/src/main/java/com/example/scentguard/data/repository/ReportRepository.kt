package com.example.scentguard.data.repository

import com.example.scentguard.data.model.ReportSummary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getDailyReport(restaurantId: String): Result<ReportSummary> {
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid ID"))
        
        return try {
            val doc = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("summaries")
                .document("daily")
                .get()
                .await()
                
            val summary = doc.toObject(ReportSummary::class.java) ?: ReportSummary()
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeeklyReport(restaurantId: String): Result<ReportSummary> {
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid ID"))
        
        return try {
            val doc = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("summaries")
                .document("weekly")
                .get()
                .await()
                
            val summary = doc.toObject(ReportSummary::class.java) ?: ReportSummary()
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
