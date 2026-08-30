package com.example.scentguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scentguard.ScentGuardApplication
import com.example.scentguard.data.model.AlertSound
import com.example.scentguard.service.AlertAudioManager
import com.example.scentguard.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val alertAudioManager: AlertAudioManager
) : AndroidViewModel(application) {
    
    private val app = application as ScentGuardApplication
    private val preferencesManager = app.preferencesManager
    private val userRepository = app.userRepository

    private val _thresholdUpdateState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val thresholdUpdateState: StateFlow<Resource<Unit>> = _thresholdUpdateState

    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    val selectedAlarmSoundId: StateFlow<String> = preferencesManager.selectedAlarmSoundId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "critical_alarm")

    val themeMode: StateFlow<String> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val gasAlertsEnabled: StateFlow<Boolean> = preferencesManager.isNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Using same notification toggle for simplicity in Phase 1
    val fanAlertsEnabled: StateFlow<Boolean> = gasAlertsEnabled

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun toggleGasAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun toggleFanAlerts(enabled: Boolean) {
        // Shared toggle for now
        toggleGasAlerts(enabled)
    }

    fun setAlarmSound(soundId: String) {
        viewModelScope.launch {
            preferencesManager.setAlarmSoundId(soundId)
        }
    }

    fun togglePreview(soundId: String) {
        if (_isPreviewPlaying.value) {
            alertAudioManager.stopAlarm()
            _isPreviewPlaying.value = false
        } else {
            val sound = AlertSound.getById(soundId)
            _isPreviewPlaying.value = true
            alertAudioManager.startPreview(sound.resId) {
                _isPreviewPlaying.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        alertAudioManager.stopAlarm()
    }

    fun updateThresholds(restaurantId: String, warn: Int, danger: Int) {
        viewModelScope.launch {
            _thresholdUpdateState.value = Resource.Loading()
            val result = userRepository.updateThresholds(restaurantId, warn, danger)
            if (result.isSuccess) {
                _thresholdUpdateState.value = Resource.Success(Unit)
            } else {
                _thresholdUpdateState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Update failed")
            }
        }
    }
    
    fun resetUpdateState() {
        _thresholdUpdateState.value = Resource.Idle()
    }
}
