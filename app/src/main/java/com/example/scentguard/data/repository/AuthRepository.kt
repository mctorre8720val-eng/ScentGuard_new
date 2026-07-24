package com.example.scentguard.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() }
) {
    private val TAG = "AuthRepository"

    private val auth by lazy { 
        try {
            authProvider()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth initialization failed", e)
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    /**
     * Provides a reactive stream of the current authenticated user.
     * Emits the user whenever the auth state changes (login, logout, session restoration).
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            Log.w(TAG, "AuthStateFlow: Firebase Auth not initialized")
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener {
            Log.d(TAG, "AuthState changed: ${it.currentUser?.uid ?: "No User"}")
            trySend(it.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        
        awaitClose {
            Log.d(TAG, "AuthStateFlow: Removing listener")
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser?> {
        Log.d(TAG, "Attempting login for: $email")
        return try {
            val firebaseAuth = auth ?: return Result.failure(Exception("Firebase not initialized"))
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed for $email", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser?> {
        Log.d(TAG, "Attempting sign up for: $email")
        return try {
            val firebaseAuth = auth ?: return Result.failure(Exception("Firebase not initialized"))
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign up successful: ${result.user?.uid}")
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed for $email", e)
            Result.failure(e)
        }
    }

    fun logout() {
        Log.d(TAG, "Logging out user: ${currentUser?.uid}")
        auth?.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        Log.d(TAG, "Requesting password reset for: $email")
        return try {
            val firebaseAuth = auth ?: return Result.failure(Exception("Firebase not initialized"))
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed for $email", e)
            Result.failure(e)
        }
    }
}
