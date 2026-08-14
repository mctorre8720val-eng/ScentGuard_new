package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.NotificationItem
import com.example.scentguard.data.repository.NotificationRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<List<NotificationItem>>>(Resource.Idle())
    val notificationsState: StateFlow<Resource<List<NotificationItem>>> = _notificationsState

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _notificationsState.value = Resource.Loading()
            val result = notificationRepository.getNotifications()
            result.onSuccess {
                _notificationsState.value = Resource.Success(it)
            }.onFailure {
                _notificationsState.value = Resource.Error(it.message ?: "Failed to load notifications")
            }
        }
    }
}
