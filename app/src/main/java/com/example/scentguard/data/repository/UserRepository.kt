package com.example.scentguard.data.repository

import android.util.Log
import com.example.scentguard.data.model.Restaurant
import com.example.scentguard.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.*
import java.util.concurrent.TimeUnit

class UserRepository(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() },
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
    private val TAG = "UserRepository"
    private val TIMEOUT_MS = 7000L // Reduced from 10s to 7s

    private val auth by lazy { try { authProvider() } catch (e: Exception) { null } }
    private val firestore by lazy { try { firestoreProvider() } catch (e: Exception) { null } }

    /**
     * Saves user profile to Firestore.
     * @param uid The UID from Firebase Auth.
     * @param user The user profile data.
     */
    suspend fun saveUserProfile(uid: String, user: User): Result<Unit> {
        Log.d(TAG, "Saving user profile for UID: $uid")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            
            val userMap = hashMapOf(
                "uid" to uid,
                "fullName" to user.fullName,
                "restaurantName" to user.restaurantName,
                "restaurantId" to user.restaurantId,
                "email" to user.email,
                "role" to user.role,
                "onboardingCompleted" to user.onboardingCompleted,
                "createdAt" to (user.createdAt ?: Timestamp.now())
            )
            
            withTimeout(TIMEOUT_MS) {
                db.collection("users").document(uid).set(userMap).await()
            }
            Log.d(TAG, "User profile saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user profile for $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Creates a new restaurant document and returns the generated invitation code.
     */
    suspend fun createRestaurant(name: String, managerUid: String): Result<Restaurant> {
        Log.d(TAG, "Creating restaurant: $name for manager: $managerUid")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val restaurantId = UUID.randomUUID().toString()
            val inviteCode = generateInviteCode()
            
            val restaurant = Restaurant(
                id = restaurantId,
                name = name,
                managerUid = managerUid,
                inviteCode = inviteCode,
                createdAt = Timestamp.now()
            )
            
            withTimeout(TIMEOUT_MS) {
                db.collection("restaurants").document(restaurantId).set(restaurant).await()
            }
            Log.d(TAG, "Restaurant created successfully with code: $inviteCode")
            Result.success(restaurant)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create restaurant $name", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves a restaurant by its unique invitation code.
     */
    suspend fun getRestaurantByInviteCode(inviteCode: String): Result<Restaurant?> {
        Log.d(TAG, "Checking invite code: $inviteCode")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val query = withTimeout(TIMEOUT_MS) {
                db.collection("restaurants")
                    .whereEqualTo("inviteCode", inviteCode.uppercase())
                    .get()
                    .await()
            }
            
            if (query.isEmpty) {
                Log.w(TAG, "No restaurant found for code: $inviteCode")
                return Result.success(null)
            }

            val restaurant = try {
                query.documents.firstOrNull()?.toObject(Restaurant::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Restaurant object", e)
                null
            }
            
            Result.success(restaurant)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching restaurant by code: $inviteCode", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves all staff members belonging to a specific restaurant.
     */
    suspend fun getStaffByRestaurant(restaurantId: String): Result<List<User>> {
        Log.d(TAG, "Fetching staff for restaurant ID: $restaurantId")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val query = withTimeout(TIMEOUT_MS) {
                db.collection("users")
                    .whereEqualTo("restaurantId", restaurantId)
                    .whereEqualTo("role", "Staff")
                    .get()
                    .await()
            }
            
            val staff = try {
                query.toObjects(User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse staff list", e)
                emptyList<User>()
            }
            
            Log.d(TAG, "Found ${staff.size} staff members")
            Result.success(staff)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching staff for restaurant $restaurantId", e)
            Result.failure(e)
        }
    }

    /**
     * Removes a staff member from a restaurant by clearing their restaurantId.
     */
    suspend fun removeStaffFromRestaurant(uid: String): Result<Unit> {
        Log.d(TAG, "Removing staff UID: $uid from restaurant")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            withTimeout(TIMEOUT_MS) {
                db.collection("users").document(uid).update(
                    mapOf(
                        "restaurantId" to "",
                        "restaurantName" to "No Restaurant"
                    )
                ).await()
            }
            Log.d(TAG, "Staff removed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove staff $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves a restaurant by its ID.
     */
    suspend fun getRestaurantById(id: String): Result<Restaurant?> {
        Log.d(TAG, "Fetching restaurant info for ID: $id")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val snapshot = withTimeout(TIMEOUT_MS) {
                db.collection("restaurants").document(id).get().await()
            }
            
            val restaurant = try {
                snapshot.toObject(Restaurant::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Restaurant info", e)
                null
            }
            
            Result.success(restaurant)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching restaurant $id", e)
            Result.failure(e)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * Updates the onboarding status for the current user.
     */
    suspend fun updateOnboardingStatus(completed: Boolean): Result<Unit> {
        Log.d(TAG, "Updating onboarding status to: $completed")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val uid = auth?.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            withTimeout(TIMEOUT_MS) {
                db.collection("users").document(uid).update("onboardingCompleted", completed).await()
            }
            Log.d(TAG, "Onboarding status updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update onboarding status", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<User?> {
        val uid = auth?.currentUser?.uid
        Log.d(TAG, "Fetching user profile for UID: $uid")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            if (uid == null) return Result.failure(Exception("User not authenticated"))
            
            val snapshot = withTimeout(TIMEOUT_MS) {
                db.collection("users").document(uid).get().await()
            }
            
            if (!snapshot.exists()) {
                Log.w(TAG, "User profile not found in Firestore for UID: $uid")
                return Result.success(null)
            }

            val user = try {
                snapshot.toObject(User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse User object for $uid", e)
                null
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile for $uid", e)
            Result.failure(e)
        }
    }
}
