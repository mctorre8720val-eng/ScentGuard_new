package com.example.scentguard.ui.screens.provisioning

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.ui.components.ScentGuardButton
import com.example.scentguard.ui.components.ScentGuardCard
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.ViewModelFactory

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisioningScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModel: ProvisioningViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val state by viewModel.state.collectAsState()
    val session by mainViewModel.userSession.collectAsState()
    
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wi-Fi Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = state) {
                is ProvisioningState.Idle -> {
                    InfoSection("Connecting to Hardware", "Bring your ESP32 near your phone and make sure Bluetooth is enabled.")
                    Spacer(Modifier.height(32.dp))
                    ScentGuardButton(text = "Scan for Device", onClick = { viewModel.startScan() })
                }
                
                is ProvisioningState.Scanning -> {
                    CircularProgressIndicator(Modifier.size(64.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Searching for ScentGuard...", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.stopScan() }) { Text("Cancel") }
                }

                is ProvisioningState.DeviceDiscovered -> {
                    Icon(Icons.Outlined.BluetoothConnected, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    val deviceName = try { s.device.name ?: "Unknown ESP32" } catch(e: SecurityException) { "ScentGuard Device" }
                    Text("Device Found: $deviceName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Spacer(Modifier.height(32.dp))
                    
                    ScentGuardCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Enter Restaurant Wi-Fi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = ssid,
                            onValueChange = { ssid = it },
                            label = { Text("Wi-Fi Name (SSID)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Wi-Fi Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        ScentGuardButton(
                            text = "Connect Device",
                            enabled = ssid.isNotBlank() && session?.restaurantId != null,
                            onClick = { 
                                session?.restaurantId?.let { rid ->
                                    viewModel.connectAndProvision(s.device, ssid, password, rid) 
                                }
                            }
                        )
                    }
                }

                is ProvisioningState.Connecting, 
                is ProvisioningState.Transferring,
                is ProvisioningState.Verifying -> {
                    CircularProgressIndicator(Modifier.size(64.dp))
                    Spacer(Modifier.height(24.dp))
                    val statusText = when(s) {
                        is ProvisioningState.Connecting -> "Connecting to ESP32..."
                        is ProvisioningState.Transferring -> "Sending Credentials..."
                        else -> "Verifying Wi-Fi Connection..."
                    }
                    Text(statusText, style = MaterialTheme.typography.titleMedium)
                }

                is ProvisioningState.Success -> {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF34C759), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Device Connected!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Your ScentGuard is now online.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(32.dp))
                    ScentGuardButton(text = "Go to Dashboard", onClick = { navController.popBackStack() })
                }

                is ProvisioningState.Error -> {
                    Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Setup Failed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(32.dp))
                    ScentGuardButton(text = "Try Again", onClick = { viewModel.startScan() })
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, body: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.WifiTethering, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
