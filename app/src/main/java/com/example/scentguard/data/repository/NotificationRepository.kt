package com.example.scentguard.data.repository

import com.example.scentguard.data.model.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Provides a real-time stream of notifications for a specific restaurant.
     */
    fun getNotificationsFlow(restaurantId: String): Flow<List<NotificationItem>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = firestore.collection("restaurants")
            .document(restaurantId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.toObjects(NotificationItem::class.java) ?: emptyList()
                trySend(items)
            }
            
        awaitClose { listener.remove() }
    }
}
