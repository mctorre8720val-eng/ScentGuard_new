package com.example.scentguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.ScentGuardApplication
import com.example.scentguard.data.model.User
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val preferencesManager = (application as ScentGuardApplication).preferencesManager

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Idle())
    val userProfile: StateFlow<Resource<User>> = _userProfile

    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted

    private val _isUserAuthenticated = MutableStateFlow(authRepository.currentUser != null)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated

    init {
        observeAuthState()
        loadLocalOnboardingStatus()
    }

    private fun loadLocalOnboardingStatus() {
        viewModelScope.launch {
            _onboardingCompleted.value = preferencesManager.isOnboardingCompleted.first()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collectLatest { firebaseUser ->
                _isUserAuthenticated.value = firebaseUser != null
                if (firebaseUser != null) {
                    fetchUserProfile()
                } else {
                    // Logout or No user
                    _userProfile.value = Resource.Error("Not authenticated")
                    _onboardingCompleted.value = false
                }
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfile.value = Resource.Loading()
            val result = withContext(Dispatchers.IO) {
                userRepository.getUserProfile()
            }
            
            result.onSuccess { user ->
                if (user != null) {
                    _userProfile.value = Resource.Success(user)
                    // Sync local cache with Firestore source of truth
                    _onboardingCompleted.value = user.onboardingCompleted
                    preferencesManager.setOnboardingCompleted(user.onboardingCompleted)
                } else {
                    // Specific error for missing documents
                    _userProfile.value = Resource.Error("MISSING_PROFILE")
                }
            }.onFailure {
                _userProfile.value = Resource.Error(it.message ?: "Failed to fetch profile")
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                userRepository.updateOnboardingStatus(true)
            }
            if (result.isSuccess) {
                preferencesManager.setOnboardingCompleted(true)
                _onboardingCompleted.value = true
                // Refresh profile to ensure state consistency
                fetchUserProfile()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // Reset local states
            _userProfile.value = Resource.Idle()
            _onboardingCompleted.value = false
            preferencesManager.setOnboardingCompleted(false)
        }
    }
}
