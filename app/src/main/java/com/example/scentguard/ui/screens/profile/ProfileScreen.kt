package com.example.scentguard.ui.screens.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.data.model.UserProfile
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
                                style = MaterialTheme.typography.headlineMedium,
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
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ProfileHeader(user)
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    SectionTitle("Account Details")
                    
                    val infoItems = mutableListOf<Any>()
                    infoItems.add(ProfileInfoItem("Full Name", user?.fullName ?: "N/A", Icons.Outlined.Person))
                    infoItems.add(ProfileInfoContent(user?.email ?: "N/A", Icons.Outlined.Email))
                    infoItems.add(ProfileInfoItem("Role", user?.role ?: "Staff", Icons.Outlined.Badge))
                    infoItems.add(ProfileInfoItem("Restaurant", user?.restaurantName ?: "N/A", Icons.Outlined.Restaurant))
                    
                    if (user?.role == "Manager") {
                        infoItems.add(ProfileInfoItem("Invite Code", "Check Staff Section", Icons.Outlined.VpnKey, isClickable = true))
                    }
                    
                    ProfileInfoCard(items = infoItems)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SectionTitle("Security \u0026 Alerts")
                    ProfileInfoCard(
                        items = listOf(
                            ProfileInfoItem("Security", "Password & Auth", Icons.Outlined.Security, isClickable = true),
                            ProfileInfoItem("Notifications", "Alert preferences", Icons.Outlined.NotificationsActive, isClickable = true)
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            mainViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = CircleShape,
                        elevation = null
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

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
fun ProfileHeader(user: UserProfile?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = user?.fullName?.take(1)?.uppercase() ?: "G",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.fullName ?: "Guest User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = user?.role ?: "Staff Member",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
fun ProfileInfoCard(items: List<Any>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column {
            items.forEachIndexed { index, item ->
                when (item) {
                    is ProfileInfoItem -> ProfileInfoRow(item.label, item.value, item.icon, item.isClickable)
                    is ProfileInfoContent -> ProfileInfoRow("Email", item.value, item.icon, false)
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: ImageVector, isClickable: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        if (isClickable) {
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
    }
}

data class ProfileInfoItem(val label: String, val value: String, val icon: ImageVector, val isClickable: Boolean = false)
data class ProfileInfoContent(val value: String, val icon: ImageVector)
