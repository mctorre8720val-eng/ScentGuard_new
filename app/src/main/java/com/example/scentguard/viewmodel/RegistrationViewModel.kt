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
        // Basic Validation
        if (fullName.isBlank() || restaurantInput.isBlank() || email.isBlank() || role.isBlank() || password.isBlank()) {
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
            _statusMessage.value = "Starting registration..."
            
            try {
                var finalRestaurantId = ""
                var finalRestaurantName = ""

                // 1. Role-Specific Logic
                if (role == "Staff") {
                    _statusMessage.value = "Validating invitation code..."
                    val restaurantResult = withContext(Dispatchers.IO) {
                        userRepository.getRestaurantByInviteCode(restaurantInput)
                    }
                    
                    if (restaurantResult.isSuccess && restaurantResult.getOrNull() != null) {
                        val restaurant = restaurantResult.getOrNull()!!
                        finalRestaurantId = restaurant.id
                        finalRestaurantName = restaurant.name
                    } else {
                        _registrationState.value = Resource.Error("Invalid or expired invitation code")
                        _statusMessage.value = ""
                        return@launch
                    }
                }

                // 2. Create Firebase Auth Account
                _statusMessage.value = "Creating your account..."
                val authResult = withContext(Dispatchers.IO) {
                    authRepository.signUp(email, password)
                }

                if (authResult.isSuccess) {
                    val firebaseUser = authResult.getOrNull()
                    if (firebaseUser != null) {
                        
                        // 3. Manager-Specific: Create Restaurant Entry
                        if (role == "Manager") {
                            _statusMessage.value = "Setting up restaurant workspace..."
                            val restaurantResult = withContext(Dispatchers.IO) {
                                userRepository.createRestaurant(restaurantInput, firebaseUser.uid)
                            }
                            if (restaurantResult.isSuccess) {
                                val restaurant = restaurantResult.getOrNull()!!
                                finalRestaurantId = restaurant.id
                                finalRestaurantName = restaurant.name
                            } else {
                                // ATOMICITY FAIL: Auth created, but Firestore restaurant failed
                                _registrationState.value = Resource.Error(
                                    "Account created, but failed to set up restaurant: ${restaurantResult.exceptionOrNull()?.message}"
                                )
                                _statusMessage.value = ""
                                return@launch
                            }
                        }

                        // 4. Prepare Profile Object
                        val user = User(
                            uid = firebaseUser.uid,
                            fullName = fullName,
                            restaurantName = finalRestaurantName,
                            restaurantId = finalRestaurantId,
                            email = email,
                            role = role,
                            createdAt = Timestamp.now()
                        )

                        // 5. Save to Firestore
                        _statusMessage.value = "Finalizing user profile..."
                        val dbResult = withContext(Dispatchers.IO) {
                            userRepository.saveUserProfile(firebaseUser.uid, user)
                        }

                        if (dbResult.isSuccess) {
                            _statusMessage.value = "Registration complete!"
                            _registrationState.value = Resource.Success(Unit)
                        } else {
                            // ATOMICITY FAIL: Auth created, but Firestore user profile failed
                            _registrationState.value = Resource.Error(
                                "Account created, but profile setup failed: ${dbResult.exceptionOrNull()?.message}"
                            )
                            _statusMessage.value = ""
                        }
                    } else {
                        _registrationState.value = Resource.Error("Account created but failed to retrieve user info")
                        _statusMessage.value = ""
                    }
                } else {
                    val errorMessage = authResult.exceptionOrNull()?.message ?: "Registration failed"
                    _registrationState.value = Resource.Error(errorMessage)
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
