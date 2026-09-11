package com.example.scentguard.ui.screens.sanitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.HistoryRepository
import com.example.scentguard.data.repository.UserRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SanitationViewModel(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sanitationHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val sanitationHistory: StateFlow<List<HistoryItem>> = _sanitationHistory.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    fun fetchSanitationHistory(restaurantId: String) {
        viewModelScope.launch {
            // In a real scenario, we'd filter by eventType in the repository
            // For now, we'll fetch general history and filter here if needed, 
            // but the prompt implies we should prepare the UI.
            historyRepository.getHistory(restaurantId, limit = 10).onSuccess { response ->
                _sanitationHistory.value = response.items.filter { 
                    it.eventType.contains("PUMP", ignoreCase = true) || 
                    it.title.contains("Sanitation", ignoreCase = true)
                }
            }
        }
    }

    fun updatePumpMode(restaurantId: String, mode: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = userRepository.updatePumpMode(restaurantId, mode)
            if (result.isSuccess) {
                _actionState.value = Resource.Success(Unit)
            } else {
                _actionState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to update mode")
            }
        }
    }

    fun triggerSanitation(restaurantId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = userRepository.triggerManualSanitation(restaurantId)
            if (result.isSuccess) {
                _actionState.value = Resource.Success(Unit)
            } else {
                _actionState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to trigger sanitation")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Idle()
    }
}
