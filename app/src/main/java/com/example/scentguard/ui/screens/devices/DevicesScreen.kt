package com.example.scentguard.ui.screens.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardChart
import com.example.scentguard.ui.components.ScentGuardFanControl
import com.example.scentguard.ui.components.ScentGuardFloatingNav
import com.example.scentguard.ui.components.ScentGuardNavigationDrawer
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.ReportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    reportViewModel: ReportViewModel = viewModel()
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data
    
    val chartState by reportViewModel.chartState.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ScentGuardNavigationDrawer(
        user = user,
        currentRoute = "devices",
        drawerState = drawerState,
        onNavigate = { route ->
            scope.launch { drawerState.close() }
            navController.navigate(route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onLogout = {
            scope.launch { drawerState.close() }
            mainViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Device Details",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ESP32 Device Status Card
                    DeviceStatusCard()

                    if (user?.role == "Manager") {
                        Spacer(modifier = Modifier.height(24.dp))
                        ScentGuardFanControl()
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // The Graph (UI-Ready for ESP32 Data)
                    Text(
                        text = "Real-time Gas Monitoring",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            when (val state = chartState) {
                                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                is Resource.Success -> ScentGuardChart(state.data!!)
                                is Resource.Error -> Text("Sensor offline", color = MaterialTheme.colorScheme.error)
                                else -> {}
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Device Info Section
                    DeviceInfoList()

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            ScentGuardFloatingNav(
                user = user,
                currentRoute = "devices",
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Router, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Vent-01 (ESP32)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Status: Online", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(12.dp).background(Color(0xFF4CAF50), CircleShape)) // Green online dot
        }
    }
}

@Composable
fun DeviceInfoList() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DeviceInfoItem("Connection Type", "Wi-Fi (2.4GHz)", Icons.Outlined.Wifi)
        DeviceInfoItem("IP Address", "192.168.1.42", Icons.Outlined.Dns)
        DeviceInfoItem("Firmware", "v1.2.0-stable", Icons.Outlined.Build)
        DeviceInfoItem("Uptime", "4d 12h 30m", Icons.Outlined.Timer)
    }
}

@Composable
fun DeviceInfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
