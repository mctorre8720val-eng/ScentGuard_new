package com.example.scentguard.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.data.model.User
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardFloatingNav
import com.example.scentguard.ui.components.ScentGuardNavigationDrawer
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ScentGuardNavigationDrawer(
        user = user,
        currentRoute = Screen.Profile.route,
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
                                "Profile",
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
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Profile Header
                    ProfileHeader(user)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Account Information
                    SectionTitle("Account Details")
                    ProfileInfoCard(
                        items = listOf(
                            InfoItem("Full Name", user?.fullName ?: "N/A", Icons.Outlined.Person),
                            InfoItem("Email", user?.email ?: "N/A", Icons.Outlined.Email),
                            InfoItem("Role", user?.role ?: "Staff", Icons.Outlined.Badge),
                            InfoItem("Restaurant", user?.restaurantName ?: "N/A", Icons.Outlined.Restaurant)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // App Settings / Actions
                    SectionTitle("App Settings")
                    ProfileInfoCard(
                        items = listOf(
                            InfoItem("Security", "Password & Auth", Icons.Outlined.Security, isClickable = true),
                            InfoItem("Notifications", "Alert preferences", Icons.Outlined.NotificationsActive, isClickable = true)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Danger Zone
                    Button(
                        onClick = {
                            mainViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(120.dp)) // Floating nav space
                }
            }

            // The Floating Nav
            ScentGuardFloatingNav(
                user = user,
                currentRoute = Screen.Profile.route,
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
fun ProfileHeader(user: User?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = user?.fullName?.take(1)?.uppercase() ?: "G",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.fullName ?: "Guest User",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user?.role ?: "Staff Member",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun ProfileInfoCard(items: List<InfoItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                item.icon, 
                                contentDescription = null, 
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.label, 
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            item.value, 
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (item.isClickable) {
                        Icon(
                            Icons.Outlined.ChevronRight, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class InfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val isClickable: Boolean = false
)
