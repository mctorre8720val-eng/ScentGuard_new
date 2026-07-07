package com.example.scentguard

import android.app.Application
import com.google.firebase.FirebaseApp

class ScentGuardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
