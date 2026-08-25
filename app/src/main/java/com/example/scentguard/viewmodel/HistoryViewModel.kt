package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.HistoryRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<Resource<List<HistoryItem>>>(Resource.Idle())
    val historyState: StateFlow<Resource<List<HistoryItem>>> = _historyState

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.userSession.collectLatest { session ->
                if (session != null) {
                    fetchHistory(session.restaurantId)
                } else {
                    _historyState.value = Resource.Idle()
                }
            }
        }
    }

    fun fetchHistory(restaurantId: String? = null) {
        val rid = restaurantId ?: authRepository.userSession.value?.restaurantId ?: return
        
        viewModelScope.launch {
            _historyState.value = Resource.Loading()
            val result = historyRepository.getHistory(rid)
            result.onSuccess {
                _historyState.value = Resource.Success(it)
            }.onFailure {
                _historyState.value = Resource.Error(it.message ?: "Failed to load history")
            }
        }
    }
}
