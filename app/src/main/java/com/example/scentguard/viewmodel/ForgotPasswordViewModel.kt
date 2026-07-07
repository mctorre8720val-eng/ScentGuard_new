package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _resetState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val resetState: StateFlow<Resource<Unit>> = _resetState

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetState.value = Resource.Error("Email cannot be empty")
            return
        }

        viewModelScope.launch {
            _resetState.value = Resource.Loading()
            val result = authRepository.resetPassword(email)
            result.onSuccess {
                _resetState.value = Resource.Success(Unit)
            }.onFailure {
                _resetState.value = Resource.Error(it.message ?: "Failed to send reset email")
            }
        }
    }
}
