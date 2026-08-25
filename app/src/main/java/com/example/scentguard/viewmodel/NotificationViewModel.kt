package com.example.scentguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.data.model.NotificationItem
import com.example.scentguard.data.repository.AuthRepository
import com.example.scentguard.data.repository.NotificationRepository
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<List<NotificationItem>>>(Resource.Idle())
    val notificationsState: StateFlow<Resource<List<NotificationItem>>> = _notificationsState

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            authRepository.userSession.collectLatest { session ->
                if (session != null) {
                    _notificationsState.value = Resource.Loading()
                    notificationRepository.getNotificationsFlow(session.restaurantId)
                        .catch { e ->
                            _notificationsState.value = Resource.Error(e.message ?: "Error loading notifications")
                        }
                        .collect { items ->
                            _notificationsState.value = Resource.Success(items)
                        }
                } else {
                    _notificationsState.value = Resource.Idle()
                }
            }
        }
    }

    fun fetchNotifications() {
        // Since we are using a real-time flow, manual fetch just resets if needed, 
        // but the flow handles updates automatically.
    }
}
