package com.example.scentguard.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() }
) {
    private val auth by lazy { 
        try {
            authProvider()
        } catch (e: Exception) {
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
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val firebaseAuth = auth ?: return Result.failure(Exception("Firebase not initialized"))
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val firebaseAuth = auth ?: return Result.failure(Exception("Firebase not initialized"))
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth?.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val firebaseAuth = auth ?: return Result.failure(Exception("Firebase not initialized"))
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
