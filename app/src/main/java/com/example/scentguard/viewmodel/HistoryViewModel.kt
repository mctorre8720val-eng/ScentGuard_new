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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _selectedDateRange = MutableStateFlow("All")
    val selectedDateRange: StateFlow<String> = _selectedDateRange

    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var hasReachedEnd = false
    private val pageSize = 50L

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.userSession.collectLatest { session ->
                if (session != null) {
                    refreshHistory()
                } else {
                    _historyState.value = Resource.Idle()
                }
            }
        }
    }

    fun setCategory(category: String) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        refreshHistory()
    }

    fun setDateRange(range: String) {
        if (_selectedDateRange.value == range) return
        _selectedDateRange.value = range
        refreshHistory()
    }

    fun refreshHistory() {
        val rid = authRepository.userSession.value?.restaurantId ?: return
        lastDocument = null
        hasReachedEnd = false
        
        viewModelScope.launch {
            _historyState.value = Resource.Loading()
            _isRefreshing.value = true
            
            val result = historyRepository.getHistory(
                restaurantId = rid,
                limit = pageSize,
                category = _selectedCategory.value,
                startDate = calculateStartDate(_selectedDateRange.value)
            )
            
            result.onSuccess { snapshot ->
                val logs = snapshot.toObjects(HistoryItem::class.java)
                _historyState.value = Resource.Success(logs)
                lastDocument = snapshot.documents.lastOrNull()
                hasReachedEnd = logs.size < pageSize
            }.onFailure {
                _historyState.value = Resource.Error(it.message ?: "Failed to load history")
            }
            _isRefreshing.value = false
        }
    }

    fun loadNextPage() {
        if (hasReachedEnd || _isLoadingMore.value || _historyState.value !is Resource.Success) return
        val rid = authRepository.userSession.value?.restaurantId ?: return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            val result = historyRepository.getHistory(
                restaurantId = rid,
                limit = pageSize,
                lastDocument = lastDocument,
                category = _selectedCategory.value,
                startDate = calculateStartDate(_selectedDateRange.value)
            )
            
            result.onSuccess { snapshot ->
                val newLogs = snapshot.toObjects(HistoryItem::class.java)
                val currentLogs = (_historyState.value as Resource.Success).data ?: emptyList()
                _historyState.value = Resource.Success(currentLogs + newLogs)
                lastDocument = snapshot.documents.lastOrNull()
                hasReachedEnd = newLogs.size < pageSize
            }
            _isLoadingMore.value = false
        }
    }

    private fun calculateStartDate(range: String): java.util.Date? {
        val calendar = java.util.Calendar.getInstance()
        return when (range) {
            "Today" -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                calendar.time
            }
            "Last 7 Days" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                calendar.time
            }
            "Last 30 Days" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
                calendar.time
            }
            else -> null
        }
    }

    fun fetchHistory(restaurantId: String? = null) {
        refreshHistory()
    }
}
