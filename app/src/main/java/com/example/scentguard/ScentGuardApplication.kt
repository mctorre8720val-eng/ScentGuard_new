package com.example.scentguard

import android.app.Application
import com.example.scentguard.data.local.PreferencesManager
import com.google.firebase.FirebaseApp

class ScentGuardApplication : Application() {
    
    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        preferencesManager = PreferencesManager(this)
    }
}
