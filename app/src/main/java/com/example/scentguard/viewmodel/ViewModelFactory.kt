package com.example.scentguard.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scentguard.ScentGuardApplication

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = application as ScentGuardApplication
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(app, app.authRepository, app.userRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(app.authRepository) as T
            }
            modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> {
                ForgotPasswordViewModel(app.authRepository) as T
            }
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                RegistrationViewModel(app.authRepository, app.userRepository) as T
            }
            modelClass.isAssignableFrom(StaffViewModel::class.java) -> {
                StaffViewModel(app.userRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(app) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(app.historyRepository) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(app.notificationRepository) as T
            }
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(app.reportRepository, app.chartRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: \${modelClass.name}")
        }
    }
}
