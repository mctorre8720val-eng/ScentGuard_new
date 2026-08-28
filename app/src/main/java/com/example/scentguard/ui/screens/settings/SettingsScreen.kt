package com.example.scentguard.ui.screens.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardFloatingNav
import com.example.scentguard.ui.components.ScentGuardNavigationDrawer
import com.example.scentguard.utils.Resource
import com.example.scentguard.utils.responsiveContainer
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.SettingsViewModel
import com.example.scentguard.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data
    val liveData by mainViewModel.liveRestaurantData.collectAsState()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val gasAlerts by viewModel.gasAlertsEnabled.collectAsState()
    val fanAlerts by viewModel.fanAlertsEnabled.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("App Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeOption("System Default", themeMode == "system") { viewModel.setThemeMode("system"); showThemeDialog = false }
                    ThemeOption("Light", themeMode == "light") { viewModel.setThemeMode("light"); showThemeDialog = false }
                    ThemeOption("Dark", themeMode == "dark") { viewModel.setThemeMode("dark"); showThemeDialog = false }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    ScentGuardNavigationDrawer(
        user = user,
        currentRoute = Screen.Settings.route,
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
                                "Settings",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
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
                        .responsiveContainer(maxWidth = 480.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SectionTitle("Appearance")
                    SettingsCard {
                        ActionItem(
                            label = "App Theme",
                            description = themeMode.replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.Palette,
                            onClick = { showThemeDialog = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    SectionTitle("Notifications")
                    SettingsCard {
                        ToggleItem(
                            label = "Air Quality Alerts",
                            description = "Notify when levels are high",
                            icon = Icons.Outlined.Warning,
                            checked = gasAlerts,
                            onCheckedChange = { viewModel.toggleGasAlerts(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                        ToggleItem(
                            label = "Fan Activity",
                            description = "Notify when fan is active",
                            icon = Icons.Outlined.Air,
                            checked = fanAlerts,
                            onCheckedChange = { viewModel.toggleFanAlerts(it) }
                        )
                    }

                    // Move Hardware Configuration to be more prominent
                    if (user != null) {
                        Spacer(modifier = Modifier.height(32.dp))
                        SectionTitle("Hardware Configuration")
                        ThresholdConfigCard(
                            restaurantId = user.restaurantId,
                            isManager = user.role.uppercase() == "MANAGER",
                            liveData = liveData,
                            viewModel = viewModel
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    SectionTitle("System")
                    SettingsCard {
                        ActionItem(
                            label = "Calibration",
                            description = "Last sync: 15d ago",
                            icon = Icons.Outlined.SettingsSuggest,
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                        ActionItem(
                            label = "About",
                            description = "ScentGuard v1.1.0",
                            icon = Icons.Outlined.Info,
                            onClick = { }
                        )
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            ScentGuardFloatingNav(
                user = user,
                currentRoute = Screen.Settings.route,
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
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(content = content)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun ThresholdConfigCard(
    restaurantId: String,
    isManager: Boolean,
    liveData: com.example.scentguard.data.model.Restaurant?,
    viewModel: SettingsViewModel
) {
    var warnVal by remember(liveData) { mutableStateOf((liveData?.thresholdWarn ?: 1000).toString()) }
    var dangerVal by remember(liveData) { mutableStateOf((liveData?.thresholdDanger ?: 1500).toString()) }
    
    val updateState by viewModel.thresholdUpdateState.collectAsState()
    
    SettingsCard {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Gas Sensitivity (PPM)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = warnVal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) warnVal = it },
                    label = { Text("WARN") },
                    modifier = Modifier.weight(1f),
                    enabled = isManager && updateState !is Resource.Loading,
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF9500),
                        unfocusedBorderColor = Color(0xFFFF9500).copy(alpha = 0.2f)
                    )
                )
                OutlinedTextField(
                    value = dangerVal,
                    onValueChange = { if (it.all { char -> char.isDigit() }) dangerVal = it },
                    label = { Text("DANGER") },
                    modifier = Modifier.weight(1f),
                    enabled = isManager && updateState !is Resource.Loading,
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF3B30),
                        unfocusedBorderColor = Color(0xFFFF3B30).copy(alpha = 0.2f)
                    )
                )
            }
            
            if (isManager) {
                Spacer(modifier = Modifier.height(24.dp))
                
                val isValid = (warnVal.toIntOrNull() ?: 0) < (dangerVal.toIntOrNull() ?: 0)
                
                Button(
                    onClick = { 
                        viewModel.updateThresholds(restaurantId, warnVal.toInt(), dangerVal.toInt()) 
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isValid && updateState !is Resource.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (updateState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Apply Configuration", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (!isValid) {
                    Text(
                        "Warning: WARN threshold must be less than DANGER.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (updateState is Resource.Error) {
                    Text(
                        text = updateState.message ?: "Update failed",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (updateState is Resource.Success) {
                    LaunchedEffect(Unit) {
                        delay(3000)
                        viewModel.resetUpdateState()
                    }
                    Text(
                        "Thresholds updated successfully!",
                        color = Color(0xFF34C759),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Text(
                    "Note: Only Managers can modify these thresholds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleItem(
    label: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ActionItem(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}
