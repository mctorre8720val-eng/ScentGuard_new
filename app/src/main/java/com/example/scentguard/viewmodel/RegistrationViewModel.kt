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
        if (fullName.isBlank() || restaurantName.isBlank() || email.isBlank() || role.isBlank() || password.isBlank()) {
            _registrationState.value = Resource.Error("All fields are required")
            return
        }

        if (password != confirmPassword) {
            _registrationState.value = Resource.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _registrationState.value = Resource.Loading()
            val authResult = withContext(Dispatchers.IO) {
                authRepository.signUp(email, password)
            }
            
            authResult.onSuccess { firebaseUser ->
                val user = User(
                    uid = firebaseUser!!.uid,
                    fullName = fullName,
                    restaurantName = restaurantName,
                    email = email,
                    role = role,
                    createdAt = Timestamp.now()
                )
                val dbResult = withContext(Dispatchers.IO) {
                    userRepository.saveUserProfile(user)
                }
                dbResult.onSuccess {
                    _registrationState.value = Resource.Success(Unit)
                }.onFailure {
                    _registrationState.value = Resource.Error(it.message ?: "Failed to save profile")
                }
            }.onFailure {
                _registrationState.value = Resource.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _registrationState.value = Resource.Idle()
    }
}
