package com.example.scentguard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.scentguard.data.model.UserProfile
import com.example.scentguard.navigation.Screen

@Composable
fun ScentGuardFloatingNav(
    user: UserProfile?,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 24.dp), // Optimized Thumb Zone
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(72.dp)
                .widthIn(max = 400.dp)
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = CircleShape,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    label = "Home",
                    route = Screen.Dashboard.route,
                    icon = Icons.Outlined.Dashboard,
                    selected = currentRoute == Screen.Dashboard.route,
                    onNavigate = onNavigate
                )
                
                NavItem(
                    label = "History",
                    route = Screen.History.route,
                    icon = Icons.Outlined.History,
                    selected = currentRoute == Screen.History.route,
                    onNavigate = onNavigate
                )

                NavItem(
                    label = "Alerts",
                    route = Screen.Notifications.route,
                    icon = Icons.Outlined.Notifications,
                    selected = currentRoute == Screen.Notifications.route,
                    onNavigate = onNavigate
                )
                NavItem(
                    label = "Profile",
                    route = Screen.Profile.route,
                    icon = Icons.Outlined.Person,
                    selected = currentRoute == Screen.Profile.route,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    route: String,
    icon: ImageVector,
    selected: Boolean,
    onNavigate: (String) -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        label = "content"
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 28.dp else 24.dp,
        label = "size"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onNavigate(route) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(iconSize),
            tint = contentColor
        )
    }
}
