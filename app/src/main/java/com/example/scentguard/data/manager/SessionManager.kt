package com.example.scentguard.data.manager

import com.example.scentguard.data.local.PreferencesManager
import com.example.scentguard.data.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SessionManager(private val preferencesManager: PreferencesManager) {

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        restoreSession()
    }

    private fun restoreSession() {
        scope.launch {
            val uid = preferencesManager.sessionUid.first()
            val role = preferencesManager.sessionRole.first()
            val restaurantId = preferencesManager.sessionRestaurantId.first()

            if (uid != null && role != null && restaurantId != null) {
                _userSession.value = UserSession(uid, role, restaurantId)
            }
        }
    }

    fun startSession(uid: String, role: String, restaurantId: String) {
        val session = UserSession(uid, role, restaurantId)
        _userSession.value = session
        scope.launch {
            preferencesManager.saveSession(uid, role, restaurantId)
        }
    }

    fun clearSession() {
        _userSession.value = null
        scope.launch {
            preferencesManager.clearSession()
        }
    }
}
