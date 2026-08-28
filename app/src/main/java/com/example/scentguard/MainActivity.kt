package com.example.scentguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.scentguard.navigation.SetupNavGraph
import com.example.scentguard.service.ScentGuardWatcherService
import com.example.scentguard.ui.theme.ScentGuardTheme
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.SettingsViewModel
import com.example.scentguard.viewmodel.ViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            // Handle permission denial if necessary
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()
        
        enableEdgeToEdge()
        setContent {
            val factory = ViewModelFactory(application)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val themeMode by settingsViewModel.themeMode.collectAsState()
            
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            ScentGuardTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val mainViewModel: MainViewModel = viewModel(factory = factory)
                    
                    // Update FCM token if logged in
                    val authState by mainViewModel.isUserAuthenticated.collectAsState()
                    LaunchedEffect(authState) {
                        if (authState) {
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    mainViewModel.updateFcmToken(token)
                                }
                            }
                        }
                    }

                    // Manage Background Watcher Service
                    val session by mainViewModel.userSession.collectAsState()
                    LaunchedEffect(session) {
                        if (session != null) {
                            val intent = Intent(this@MainActivity, ScentGuardWatcherService::class.java).apply {
                                action = ScentGuardWatcherService.ACTION_START
                                putExtra(ScentGuardWatcherService.EXTRA_RESTAURANT_ID, session?.restaurantId)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                        } else {
                            val intent = Intent(this@MainActivity, ScentGuardWatcherService::class.java).apply {
                                action = ScentGuardWatcherService.ACTION_STOP
                            }
                            stopService(intent)
                        }
                    }

                    SetupNavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        // Always good to have for BLE scanning reliability on many devices
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}
