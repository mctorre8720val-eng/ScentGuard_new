package com.example.scentguard.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.data.model.User
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardBackground
import com.example.scentguard.ui.components.ScentGuardCard
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data

    ScentGuardBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "ScentGuard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { 
                            mainViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardHeader(user)
                }
                
                item {
                    AirQualityHero()
                }
                
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatusMiniCard(
                            label = "Fan Status",
                            value = "OFF",
                            icon = Icons.Outlined.Air,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        StatusMiniCard(
                            label = "Auto Mode",
                            value = "Enabled",
                            icon = Icons.Outlined.AutoMode,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatusMiniCard(
                            label = "Temperature",
                            value = "28°C",
                            icon = Icons.Outlined.Thermostat,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        StatusMiniCard(
                            label = "Humidity",
                            value = "71%",
                            icon = Icons.Outlined.WaterDrop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    QuickActions(user?.role ?: "Staff", navController)
                }
                
                item {
                    RecentActivitySection()
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(user: User?) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Welcome back,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = user?.fullName ?: "Guest",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Icon(
                Icons.Outlined.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " ${user?.restaurantName ?: "Setting up..."}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AirQualityHero() {
    ScentGuardCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Overall Air Quality",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Excellent",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Gas Level: 185 ppm",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusMiniCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    ScentGuardCard(modifier = modifier) {
        Column {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickActions(role: String, navController: NavHostController) {
    Column {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (role == "Manager") {
                ActionButton(
                    text = "History",
                    icon = Icons.Outlined.History,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Screen.History.route) }
                
                ActionButton(
                    text = "Reports",
                    icon = Icons.Outlined.Assessment,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Screen.Reports.route) }
            } else {
                ActionButton(
                    text = "Alerts",
                    icon = Icons.Outlined.Notifications,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Screen.Notifications.route) }
                
                ActionButton(
                    text = "Profile",
                    icon = Icons.Outlined.Person,
                    modifier = Modifier.weight(1f)
                ) { navController.navigate(Screen.Profile.route) }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ScentGuardCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ActivityItem("Fan Activated", "Automatic safety trigger", "10:30 AM")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ActivityItem("Air Quality Good", "Levels stabilized", "10:45 AM")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ActivityItem("System Test", "Sensor check completed", "09:00 AM")
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
