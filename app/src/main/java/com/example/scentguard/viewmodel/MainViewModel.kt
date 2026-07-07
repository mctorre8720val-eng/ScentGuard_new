package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.User
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userProfile: StateFlow<Resource<User>> = _userProfile

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collectLatest { firebaseUser ->
                if (firebaseUser != null) {
                    fetchUserProfile()
                } else {
                    // Firebase Auth sometimes takes a moment to restore the local session.
                    // We wait 1 second before deciding the user is definitely not logged in.
                    // If the user is restored during this delay, collectLatest will cancel this 
                    // and start the fetchUserProfile() block above.
                    delay(1000)
                    if (authRepository.currentUser == null) {
                        _userProfile.value = Resource.Error("Not authenticated")
                    }
                }
            }
        }
    }

    private suspend fun fetchUserProfile() {
        _userProfile.value = Resource.Loading()
        val result = withContext(Dispatchers.IO) {
            userRepository.getUserProfile()
        }
        
        result.onSuccess {
            if (it != null) {
                _userProfile.value = Resource.Success(it)
            } else {
                _userProfile.value = Resource.Error("Profile not found")
            }
        }.onFailure {
            _userProfile.value = Resource.Error(it.message ?: "Failed to fetch profile")
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // The authStateFlow observer will automatically set _userProfile to Error/Idle
        }
    }
}
