package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.User
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Idle())
    val userProfile: StateFlow<Resource<User>> = _userProfile

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        val currentUser = authRepository.currentUser
        if (currentUser != null) {
            fetchUserProfile()
        } else {
            _userProfile.value = Resource.Error("Not authenticated")
        }
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfile.value = Resource.Loading()
            val result = userRepository.getUserProfile()
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
    }

    fun logout() {
        authRepository.logout()
        _userProfile.value = Resource.Idle()
    }
}
