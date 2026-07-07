package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.utils.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<FirebaseUser>>(Resource.Idle())
    val loginState: StateFlow<Resource<FirebaseUser>> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error("Fields cannot be empty")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = Resource.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = withContext(Dispatchers.IO) {
                authRepository.login(email, password)
            }
            result.onSuccess {
                _loginState.value = Resource.Success(it!!)
            }.onFailure {
                _loginState.value = Resource.Error(it.message ?: "Login failed")
            }
        }
    }

    fun resetState() {
        _loginState.value = Resource.Idle()
    }
}
