package com.example.scentguard.data.repository

import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.model.HistoryType
import com.example.scentguard.data.model.Incident
import com.example.scentguard.data.model.StaffAction
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HistoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getHistory(
        restaurantId: String,
        limit: Long = 50,
        lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null,
        category: String? = null,
        startDate: java.util.Date? = null
    ): Result<com.google.firebase.firestore.QuerySnapshot> {
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid Restaurant ID"))
        
        return try {
            var query = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)

            if (category != null && category != "All") {
                when (category) {
                    "Alerts" -> query = query.whereIn("type", listOf("ALERT", "WARNING"))
                    "Fan" -> query = query.whereIn("eventType", listOf("FAN_ON", "FAN_OFF"))
                    "Devices" -> query = query.whereIn("eventType", listOf("DEVICE_CONNECT", "DEVICE_DISCONNECT"))
                    "Users" -> query = query.whereIn("eventType", listOf("USER_LOGIN", "USER_CHANGE", "MEMBER_JOIN", "MEMBER_REMOVE"))
                    "System" -> query = query.whereIn("eventType", listOf("SYSTEM_START", "SYSTEM_UPDATE", "AIR_SAFE"))
                }
            }

            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", com.google.firebase.Timestamp(startDate))
            }

            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }

            val snapshot = query.limit(limit).get().await()
            Result.success(snapshot)
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

    /**
     * Queries the incidents sub-collection for any document that is not yet CLEARED.
     */
    suspend fun getActiveIncident(restaurantId: String): Result<Incident?> {
        if (restaurantId.isBlank()) return Result.failure(Exception("Invalid Restaurant ID"))
        return try {
            val snapshot = firestore.collection("restaurants")
                .document(restaurantId)
                .collection("incidents")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            
            val incident = snapshot.documents
                .mapNotNull { it.toObject(Incident::class.java) }
                .firstOrNull { it.status == "IN_PROGRESS" }
            
            Result.success(incident)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atomically adds a staff response message to an active incident.
     */
    suspend fun addStaffResponse(restaurantId: String, incidentId: String, response: StaffAction): Result<Unit> {
        if (restaurantId.isBlank() || incidentId.isBlank()) return Result.failure(Exception("Invalid data"))
        return try {
            firestore.collection("restaurants")
                .document(restaurantId)
                .collection("incidents")
                .document(incidentId)
                .update("actions", FieldValue.arrayUnion(response))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
