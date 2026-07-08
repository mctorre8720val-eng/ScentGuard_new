package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.User
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _registrationState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val registrationState: StateFlow<Resource<Unit>> = _registrationState

    fun register(
        fullName: String,
        restaurantName: String,
        email: String,
        role: String,
        password: String,
        confirmPassword: String
    ) {
        // Basic Validation
        if (fullName.isBlank() || restaurantName.isBlank() || email.isBlank() || role.isBlank() || password.isBlank()) {
            _registrationState.value = Resource.Error("All fields are required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registrationState.value = Resource.Error("Please enter a valid email address")
            return
        }

        if (password.length < 6) {
            _registrationState.value = Resource.Error("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            _registrationState.value = Resource.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            
            try {
                // 1. Create Firebase Auth Account
                val authResult = withContext(Dispatchers.IO) {
                    authRepository.signUp(email, password)
                }

                if (authResult.isSuccess) {
                    val firebaseUser = authResult.getOrNull()
                    if (firebaseUser != null) {
                        // 2. Prepare Profile Object
                        val user = User(
                            uid = firebaseUser.uid,
                            fullName = fullName,
                            restaurantName = restaurantName,
                            email = email,
                            role = role,
                            createdAt = Timestamp.now()
                        )

                        // 3. Save to Firestore
                        val dbResult = withContext(Dispatchers.IO) {
                            userRepository.saveUserProfile(firebaseUser.uid, user)
                        }

                        if (dbResult.isSuccess) {
                            // Success! We stay logged in as per MCP.md guidelines for Dashboard navigation
                            _registrationState.value = Resource.Success(Unit)
                        } else {
                            // If saving profile fails, we should still probably inform the user
                            // but the account was created.
                            _registrationState.value = Resource.Error(
                                dbResult.exceptionOrNull()?.message ?: "Account created but profile save failed"
                            )
                        }
                    } else {
                        _registrationState.value = Resource.Error("Account created but failed to retrieve user info")
                    }
                } else {
                    _registrationState.value = Resource.Error(
                        authResult.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                // Final safety net
                _registrationState.value = Resource.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun resetState() {
        _registrationState.value = Resource.Idle()
    }
}
