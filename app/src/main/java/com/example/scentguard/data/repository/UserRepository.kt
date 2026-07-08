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

    /**
     * Saves user profile to Firestore.
     * @param uid The UID from Firebase Auth.
     * @param user The user profile data.
     */
    suspend fun saveUserProfile(uid: String, user: User): Result<Unit> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firebase not initialized"))
            // Using a simple map for initial save to avoid potential serialization hangs
            val userMap = hashMapOf(
                "uid" to uid,
                "fullName" to user.fullName,
                "restaurantName" to user.restaurantName,
                "email" to user.email,
                "role" to user.role,
                "onboardingCompleted" to user.onboardingCompleted,
                "createdAt" to (user.createdAt ?: com.google.firebase.Timestamp.now())
            )
            db.collection("users").document(uid).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the onboarding status for the current user.
     */
    suspend fun updateOnboardingStatus(completed: Boolean): Result<Unit> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firebase not initialized"))
            val uid = auth?.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            db.collection("users").document(uid).update("onboardingCompleted", completed).await()
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
