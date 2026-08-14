package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.HistoryItem
import com.example.scentguard.data.repository.HistoryRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<Resource<List<HistoryItem>>>(Resource.Idle())
    val historyState: StateFlow<Resource<List<HistoryItem>>> = _historyState

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _historyState.value = Resource.Loading()
            val result = historyRepository.getHistory()
            result.onSuccess {
                _historyState.value = Resource.Success(it)
            }.onFailure {
                _historyState.value = Resource.Error(it.message ?: "Failed to load history")
            }
        }
    }
}
