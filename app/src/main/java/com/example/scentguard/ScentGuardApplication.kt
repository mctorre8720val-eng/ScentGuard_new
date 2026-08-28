package com.example.scentguard

import android.app.Application
import com.example.scentguard.data.local.PreferencesManager
import com.example.scentguard.data.manager.SessionManager
import com.example.scentguard.data.repository.*
import com.google.firebase.FirebaseApp

class ScentGuardApplication : Application() {
    
    companion object {
        lateinit var instance: ScentGuardApplication
            private set
    }

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var sessionManager: SessionManager
        private set

    lateinit var userRepository: UserRepository
        private set

    lateinit var authRepository: AuthRepository
        private set
        
    lateinit var historyRepository: HistoryRepository
        private set
        
    lateinit var notificationRepository: NotificationRepository
        private set
        
    lateinit var chartRepository: ChartRepository
        private set
        
    lateinit var reportRepository: ReportRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        preferencesManager = PreferencesManager(this)
        sessionManager = SessionManager(preferencesManager)
        userRepository = UserRepository()
        authRepository = AuthRepository(sessionManager, userRepository)
        historyRepository = HistoryRepository()
        notificationRepository = NotificationRepository()
        chartRepository = ChartRepository()
        reportRepository = ReportRepository()
    }
}
