package com.example.scentguard.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.UserProfile
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
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val TAG = "RegistrationViewModel"

    private val _registrationState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val registrationState: StateFlow<Resource<Unit>> = _registrationState

    private val _statusMessage = MutableStateFlow<String>("")
    val statusMessage: StateFlow<String> = _statusMessage

    fun register(
        fullName: String,
        restaurantInput: String, // Can be Name (Manager) or Invite Code (Staff)
        email: String,
        role: String,
        password: String,
        confirmPassword: String
    ) {
        val trimmedFullName = fullName.trim()
        val trimmedRestaurantInput = restaurantInput.trim()
        val trimmedEmail = email.trim()
        val trimmedRole = role.trim()

        Log.d(TAG, "Attempting registration for email: ${trimmedEmail.take(3)}***")

        val isAuthenticated = authRepository.currentUser != null

        // Basic Validation
        if (trimmedFullName.isBlank() || trimmedRestaurantInput.isBlank() || trimmedEmail.isBlank() || trimmedRole.isBlank()) {
            _registrationState.value = Resource.Error("All fields are required")
            return
        }

        if (!isAuthenticated && password.isBlank()) {
            _registrationState.value = Resource.Error("Password is required for new accounts")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            Log.w(TAG, "Validation failed: Invalid email format for '${trimmedEmail.take(3)}***'")
            _registrationState.value = Resource.Error("Please enter a valid email address")
            return
        }

        if (!isAuthenticated) {
            if (password.length < 6) {
                _registrationState.value = Resource.Error("Password must be at least 6 characters")
                return
            }

            if (password != confirmPassword) {
                _registrationState.value = Resource.Error("Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            _statusMessage.value = "Starting registration..."
            
            try {
                var finalRestaurantId = ""
                var finalRestaurantName = ""

                // 1. Role-Specific Logic
                if (trimmedRole == "Staff") {
                    _statusMessage.value = "Validating invitation code..."
                    val restaurantResult = withContext(Dispatchers.IO) {
                        userRepository.getRestaurantByInviteCode(trimmedRestaurantInput)
                    }
                    
                    if (restaurantResult.isSuccess && restaurantResult.getOrNull() != null) {
                        val restaurant = restaurantResult.getOrNull()!!
                        finalRestaurantId = restaurant.id
                        finalRestaurantName = restaurant.name
                    } else {
                        val errorMessage = if (restaurantResult.isFailure) {
                            restaurantResult.exceptionOrNull()?.message ?: "Invalid invitation code"
                        } else {
                            "Invalid invitation code"
                        }
                        _registrationState.value = Resource.Error(errorMessage)
                        _statusMessage.value = ""
                        return@launch
                    }
                }

                // 2. Auth Account Handling
                var firebaseUid = authRepository.currentUser?.uid
                
                if (firebaseUid == null) {
                    _statusMessage.value = "Creating your account..."
                    val authResult = withContext(Dispatchers.IO) {
                        authRepository.signUp(trimmedEmail, password)
                    }

                    if (authResult.isSuccess) {
                        firebaseUid = authResult.getOrNull()?.uid
                    } else {
                        val errorMessage = authResult.exceptionOrNull()?.message ?: "Registration failed"
                        _registrationState.value = Resource.Error(errorMessage)
                        _statusMessage.value = ""
                        return@launch
                    }
                }

                if (firebaseUid != null) {
                    // 3. Manager-Specific: Create Restaurant Entry
                    if (trimmedRole == "Manager") {
                        _statusMessage.value = "Setting up restaurant workspace..."
                        val restaurantResult = withContext(Dispatchers.IO) {
                            userRepository.createRestaurant(trimmedRestaurantInput, firebaseUid)
                        }
                        if (restaurantResult.isSuccess) {
                            val restaurant = restaurantResult.getOrNull()!!
                            finalRestaurantId = restaurant.id
                            finalRestaurantName = restaurant.name
                        } else {
                            _registrationState.value = Resource.Error(
                                "Failed to set up restaurant: ${restaurantResult.exceptionOrNull()?.message}"
                            )
                            _statusMessage.value = ""
                            return@launch
                        }
                    }

                    // 4. Prepare Profile Object
                    val user = UserProfile(
                        uid = firebaseUid!!,
                        fullName = trimmedFullName,
                        restaurantName = finalRestaurantName,
                        restaurantId = finalRestaurantId,
                        email = trimmedEmail,
                        role = trimmedRole.uppercase(),
                        createdAt = Timestamp.now()
                    )

                    // 5. Save to Firestore
                    _statusMessage.value = "Finalizing user profile..."
                    val dbResult = withContext(Dispatchers.IO) {
                        userRepository.saveUserProfile(firebaseUid!!, user)
                    }

                    if (dbResult.isSuccess) {
                        _statusMessage.value = "Registration complete!"
                        
                        // Update the session
                        authRepository.restoreSession()
                        
                        _registrationState.value = Resource.Success(Unit)
                    } else {
                        val error = dbResult.exceptionOrNull()
                        val message = if (error?.message?.contains("permission-denied", ignoreCase = true) == true) {
                            "Access denied. Please update Firestore security rules."
                        } else {
                            "Profile setup failed: ${error?.message}"
                        }
                        _registrationState.value = Resource.Error(message)
                        _statusMessage.value = ""
                    }
                } else {
                    _registrationState.value = Resource.Error("Failed to retrieve user identifier")
                    _statusMessage.value = ""
                }
            } catch (e: Exception) {
                _registrationState.value = Resource.Error(e.message ?: "An unexpected error occurred")
                _statusMessage.value = ""
            }
        }
    }

    fun resetState() {
        _registrationState.value = Resource.Idle()
        _statusMessage.value = ""
    }
}
