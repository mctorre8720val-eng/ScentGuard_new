package com.example.scentguard.data.repository

import android.util.Log
import com.example.scentguard.data.model.Restaurant
import com.example.scentguard.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.*
import java.util.concurrent.TimeUnit

class UserRepository(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() },
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
    private val TAG = "UserRepository"
    private val TIMEOUT_MS = 7000L

    private val auth by lazy { try { authProvider() } catch (e: Exception) { null } }
    private val firestore by lazy { try { firestoreProvider() } catch (e: Exception) { null } }

    fun getRestaurantFlow(restaurantId: String): Flow<Restaurant?> = callbackFlow {
        val db = firestore ?: run {
            close()
            return@callbackFlow
        }
        
        if (restaurantId.isBlank()) {
            trySend(null)
            return@callbackFlow
        }

        val listener = db.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to restaurant: $restaurantId", error)
                    return@addSnapshotListener
                }
                
                val restaurant = snapshot?.toObject(Restaurant::class.java)
                trySend(restaurant)
            }
            
        awaitClose { listener.remove() }
    }

    /**
     * Updates the fan mode for a restaurant.
     */
    suspend fun updateFanMode(restaurantId: String, mode: String): Result<Unit> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            db.collection("restaurants").document(restaurantId).update("fanMode", mode).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the gas safety thresholds for a restaurant.
     */
    suspend fun updateThresholds(restaurantId: String, warn: Int, danger: Int): Result<Unit> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            db.collection("restaurants").document(restaurantId).update(
                mapOf(
                    "thresholdWarn" to warn,
                    "thresholdDanger" to danger
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the user's avatar selection.
     */
    suspend fun updateAvatar(uid: String, type: String, id: String?): Result<Unit> {
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            db.collection("users").document(uid).update(
                mapOf(
                    "avatarType" to type,
                    "avatarId" to id
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves user profile to Firestore.
     * @param uid The UID from Firebase Auth.
     * @param user The user profile data.
     */
    suspend fun saveUserProfile(uid: String, user: UserProfile): Result<Unit> {
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
                "avatarType" to user.avatarType,
                "avatarId" to user.avatarId,
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
            
            // Set expiry to 24 hours from now
            val expiry = Timestamp(Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)))

            val restaurant = Restaurant(
                id = restaurantId,
                name = name,
                managerUid = managerUid,
                inviteCode = inviteCode,
                inviteCodeExpiresAt = expiry,
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
        val uppercaseCode = inviteCode.uppercase()
        Log.d(TAG, "Checking invite code: $uppercaseCode")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val query = withTimeout(TIMEOUT_MS) {
                db.collection("restaurants")
                    .whereEqualTo("inviteCode", uppercaseCode)
                    .get()
                    .await()
            }
            
            if (query.isEmpty) {
                Log.w(TAG, "No restaurant found for code: $uppercaseCode")
                return Result.success(null)
            }

            val doc = query.documents.firstOrNull()
            Log.d(TAG, "Restaurant document found: \${doc?.id}")
            
            val restaurant = try {
                doc?.toObject(Restaurant::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Restaurant object", e)
                null
            }
            
            // Check for expiration
            if (restaurant != null) {
                val expiry = restaurant.inviteCodeExpiresAt
                val now = Date()
                Log.d(TAG, "Code expiry: \${expiry?.toDate()}, Current time: \$now")
                if (expiry != null && expiry.toDate().before(now)) {
                    Log.w(TAG, "Invite code \$uppercaseCode has expired")
                    return Result.failure(Exception("Invite code has expired"))
                }
            }
            
            Result.success(restaurant)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching restaurant by code: \$uppercaseCode", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves all staff members belonging to a specific restaurant.
     */
    suspend fun getStaffByRestaurant(restaurantId: String): Result<List<UserProfile>> {
        Log.d(TAG, "Fetching staff for restaurant ID: $restaurantId")
        if (restaurantId.isBlank()) {
            return Result.failure(Exception("Restaurant ID is missing"))
        }
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val query = withTimeout(TIMEOUT_MS) {
                db.collection("users")
                    .whereEqualTo("restaurantId", restaurantId)
                    .get()
                    .await()
            }
            
            val staff = try {
                query.toObjects(UserProfile::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse staff list", e)
                emptyList<UserProfile>()
            }
            
            Log.d(TAG, "Found ${staff.size} staff members")
            Result.success(staff)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching staff for restaurant $restaurantId", e)
            val errorMessage = if (e.message?.contains("permission-denied", ignoreCase = true) == true) {
                "Permission Denied: Please apply the new Firestore Rules provided in the plan."
            } else {
                e.message ?: "Unknown database error"
            }
            Result.failure(Exception(errorMessage))
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

    suspend fun refreshInviteCode(restaurantId: String, durationHours: Long = 24): Result<String> {
        Log.d(TAG, "Refreshing invite code for restaurant: $restaurantId with duration: $durationHours hours")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            val newCode = generateInviteCode()
            val newExpiry = Timestamp(Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(durationHours)))
            
            withTimeout(TIMEOUT_MS) {
                db.collection("restaurants").document(restaurantId).update(
                    mapOf(
                        "inviteCode" to newCode,
                        "inviteCodeExpiresAt" to newExpiry
                    )
                ).await()
            }
            
            Log.d(TAG, "Invite code refreshed: $newCode")
            Result.success(newCode)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh invite code", e)
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

    /**
     * Updates the FCM token for a user.
     */
    suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        Log.d(TAG, "Updating FCM token for UID: $uid")
        return try {
            val db = firestore ?: return Result.failure(Exception("Firestore not initialized"))
            withTimeout(TIMEOUT_MS) {
                db.collection("users").document(uid).update("fcmToken", token).await()
            }
            Log.d(TAG, "FCM token updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update FCM token", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile?> {
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
                snapshot.toObject(UserProfile::class.java)
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
