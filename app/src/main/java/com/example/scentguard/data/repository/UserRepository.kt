package com.example.scentguard.data.repository

import com.example.scentguard.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() },
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
    private val auth by lazy { try { authProvider() } catch (e: Exception) { null } }
    private val firestore by lazy { try { firestoreProvider() } catch (e: Exception) { null } }

    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firebase not initialized"))
            val uid = auth?.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            db.collection("users").document(uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<User?> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firebase not initialized"))
            val uid = auth?.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val snapshot = db.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
