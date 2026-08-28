package com.example.scentguard.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.data.model.UserProfile
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardFanControl
import com.example.scentguard.ui.components.ScentGuardFloatingNav
import com.example.scentguard.ui.components.ScentGuardNavigationDrawer
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data
    val liveData by mainViewModel.liveRestaurantData.collectAsState()
    
    // Heartbeat logic: Consider "Online" if lastSeen is within 2 minutes
    var isOnline by remember { mutableStateOf(false) }
    
    LaunchedEffect(liveData?.lastSeen) {
        val lastSeenDate = liveData?.lastSeen?.toDate()
        if (lastSeenDate == null) {
            isOnline = false
        } else {
            val diffMs = System.currentTimeMillis() - lastSeenDate.time
            isOnline = diffMs < TimeUnit.MINUTES.toMillis(2)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ScentGuardNavigationDrawer(
        user = user,
        currentRoute = Screen.Dashboard.route,
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 600.dp
            
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "ScentGuard",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        DashboardHeader(user)
                    }
                    
                    if (isWideScreen) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Box(modifier = Modifier.weight(1.2f)) {
                                    AirQualityHero(
                                        gasLevel = if (isOnline) liveData?.currentGasPpm ?: 0 else 0,
                                        airStatus = liveData?.airStatus ?: "SAFE",
                                        onViewAnalytics = { navController.navigate(Screen.Reports.route) }
                                    )
                                }
                                Box(modifier = Modifier.weight(0.8f)) {
                                    StatisticsSection(isOnline = isOnline, fanStatus = liveData?.fanStatus ?: "OFF")
                                }
                            }
                        }
                    } else {
                        item {
                            AirQualityHero(
                                gasLevel = if (isOnline) liveData?.currentGasPpm ?: 0 else 0,
                                airStatus = liveData?.airStatus ?: "SAFE",
                                onViewAnalytics = { navController.navigate(Screen.Reports.route) }
                            )
                        }
                        item {
                            StatisticsSection(isOnline = isOnline, fanStatus = liveData?.fanStatus ?: "OFF")
                        }
                    }
                    
                    item {
                        MetricsGrid(
                            isWideScreen = isWideScreen,
                            gasLevel = if (isOnline) liveData?.currentGasPpm ?: 0 else 0,
                            temp = if (isOnline) liveData?.temperature ?: 0f else 0f,
                            isOnline = isOnline
                        )
                    }

                    if (user?.role?.uppercase() == "MANAGER") {
                        item {
                            ScentGuardFanControl(
                                initialMode = when(liveData?.fanMode?.uppercase()) {
                                    "ON" -> com.example.scentguard.ui.components.FanMode.ON
                                    "OFF" -> com.example.scentguard.ui.components.FanMode.OFF
                                    else -> com.example.scentguard.ui.components.FanMode.AUTO
                                },
                                onModeChange = { mode -> 
                                    mainViewModel.updateFanMode(mode.name)
                                }
                            )
                        }
                    }
                    
                    item {
                        RecentActivitySection()
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(112.dp))
                    }
                }
            }

            ScentGuardFloatingNav(
                user = user,
                currentRoute = Screen.Dashboard.route,
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
fun DashboardHeader(user: UserProfile?) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "$greeting,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = user?.fullName ?: "Guest User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
        
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = CircleShape
            ) {
                Text(
                    text = user?.role ?: "Staff",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = user?.restaurantName ?: "ScentGuard Station",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AirQualityHero(
    gasLevel: Int,
    airStatus: String,
    onViewAnalytics: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val statusText = remember(gasLevel, airStatus) {
        if (gasLevel == 0) "Standby" else airStatus.uppercase()
    }

    val statusColor = remember(airStatus, gasLevel) {
        if (gasLevel == 0) return@remember Color.Gray
        when (airStatus.uppercase()) {
            "SAFE" -> Color(0xFF34C759)
            "WARN" -> Color(0xFFFF9500)
            "DANGER" -> Color(0xFFFF3B30)
            else -> Color(0xFF34C759)
        }
    }

    Surface(
        onClick = onViewAnalytics,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Current Air Quality",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(contentAlignment = Alignment.Center) {
                // Breathing Aura
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(auraScale)
                        .background(statusColor.copy(alpha = auraAlpha), CircleShape)
                )

                CircularProgressIndicator(
                    progress = { (gasLevel / 1000f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(180.dp),
                    color = statusColor,
                    strokeWidth = 10.dp,
                    trackColor = statusColor.copy(alpha = 0.05f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "$gasLevel ppm",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (gasLevel == 0) Icons.Outlined.PowerSettingsNew else if (airStatus.uppercase() != "DANGER") Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                    null, 
                    tint = statusColor, 
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (gasLevel == 0) "Awaiting sensor data..." else if (airStatus.uppercase() != "DANGER") "Optimized ventilation" else "Ventilation recommended",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun MetricsGrid(
    isWideScreen: Boolean,
    gasLevel: Int,
    temp: Float,
    isOnline: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Hardware Status",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                label = "System Temp",
                value = temp.toInt().toString(),
                unit = "°C",
                icon = Icons.Outlined.Thermostat,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Gas Level",
                value = gasLevel.toString(),
                unit = "ppm",
                icon = Icons.Outlined.Cloud,
                modifier = Modifier.weight(1f)
            )
            if (isWideScreen) {
                MetricCard(
                    label = "System Status",
                    value = if (!isOnline) "OFFLINE" else "OK",
                    unit = "",
                    icon = Icons.Outlined.Sensors,
                    modifier = Modifier.weight(1f),
                    valueColor = if (!isOnline) MaterialTheme.colorScheme.error else Color(0xFF34C759)
                )
            }
        }
        
        if (!isWideScreen) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    label = "Sensor Unit",
                    value = if (isOnline) "Online" else "Offline",
                    unit = "",
                    icon = Icons.Outlined.Memory,
                    modifier = Modifier.weight(1f),
                    valueStyle = MaterialTheme.typography.titleLarge,
                    valueColor = if (isOnline) Color(0xFF34C759) else MaterialTheme.colorScheme.error
                )
                // Spacer card to keep grid balanced
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String, 
    value: String, 
    unit: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = valueStyle, fontWeight = FontWeight.Bold, color = valueColor)
                if (unit.isNotEmpty()) {
                    Text(unit, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun StatisticsSection(isOnline: Boolean, fanStatus: String) {
    Column {
        Text(
            "System performance",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(
                label = "Fan Status", 
                value = fanStatus.uppercase(), 
                icon = Icons.Outlined.Air, 
                Modifier.fillMaxWidth(),
                statusColor = if (fanStatus.uppercase() == "ON") Color(0xFF34C759) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            MiniStatCard("Uptime", if (isOnline) "99%" else "0%", Icons.Outlined.Timer, Modifier.fillMaxWidth())
            MiniStatCard(
                "Status", 
                if (isOnline) "Online" else "Offline", 
                Icons.Outlined.Sensors, 
                Modifier.fillMaxWidth(),
                statusColor = if (isOnline) Color(0xFF34C759) else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun MiniStatCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    statusColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = statusColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = statusColor)
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* View All */ }) {
                Text("Show all", fontWeight = FontWeight.Bold)
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ActivityItem("Fan Activated", "Auto safety trigger", "10:30 AM", Icons.Outlined.AutoMode)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ActivityItem("Air Quality Good", "Levels stabilized", "10:45 AM", Icons.Outlined.CheckCircleOutline)
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, time: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}
