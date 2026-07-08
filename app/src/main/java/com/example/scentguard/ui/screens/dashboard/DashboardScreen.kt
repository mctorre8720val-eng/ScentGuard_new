package com.example.scentguard.ui.screens.dashboard

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.data.model.User
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardFanControl
import com.example.scentguard.ui.components.ScentGuardFloatingNav
import com.example.scentguard.ui.components.ScentGuardNavigationDrawer
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data
    
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
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "ScentGuard",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Notifications */ }) {
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        DashboardHeader(user)
                    }
                    
                    item {
                        ConnectivityStatusBar(onClick = { navController.navigate("devices") })
                    }

                    item {
                        AirQualityHero(onViewAnalytics = { navController.navigate(Screen.Reports.route) })
                    }
                    
                    item {
                        MetricsGrid()
                    }

                    if (user?.role == "Manager") {
                        item {
                            ScentGuardFanControl()
                        }
                    }
                    
                    item {
                        StatisticsSection()
                    }
                    
                    item {
                        RecentActivitySection()
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(100.dp)) // Extra space for the floating bar
                    }
                }
            }

            // The Floating Nav
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
fun DashboardHeader(user: User?) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = user?.fullName ?: "Guest User",
            style = MaterialTheme.typography.headlineMedium
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = user?.role ?: "Staff",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = user?.restaurantName ?: "ABC Restaurant",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ConnectivityStatusBar(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " Connected to Vent-01",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AirQualityHero(onViewAnalytics: () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    )

    Surface(
        onClick = onViewAnalytics,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.background(gradient)) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Air Quality Status",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        "Excellent",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Gas: 185 ppm",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Analytics, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                label = "Gas Level",
                value = "185",
                unit = "ppm",
                icon = Icons.Outlined.Cloud,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Fan Status",
                value = "OFF",
                unit = "Auto",
                icon = Icons.Outlined.Air,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                label = "Temperature",
                value = "28",
                unit = "°C",
                icon = Icons.Outlined.Thermostat,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Humidity",
                value = "71",
                unit = "%",
                icon = Icons.Outlined.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatisticsSection() {
    Column {
        Text(
            "System Statistics",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard("Alerts Today", "2", Icons.Outlined.WarningAmber, Modifier.weight(1f))
            MiniStatCard("Fan Runtime", "35m", Icons.Outlined.Timer, Modifier.weight(1f))
            MiniStatCard("Sensor", "Online", Icons.Outlined.Sensors, Modifier.weight(1f))
        }
    }
}

@Composable
fun MiniStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, unit: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium
                )
                if (unit.isNotEmpty()) {
                    Text(
                        unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                    )
                }
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
                "Recent Activity",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { /* View All */ }) {
                Text("View All", style = MaterialTheme.typography.labelLarge)
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ActivityItem("Fan Activated", "Auto safety trigger", "10:30 AM", Icons.Outlined.AutoMode)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ActivityItem("Air Quality Good", "Levels stabilized", "10:45 AM", Icons.Outlined.CheckCircleOutline)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ActivityItem("System Check", "Periodic sensor sync", "09:00 AM", Icons.Outlined.Sync)
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
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
