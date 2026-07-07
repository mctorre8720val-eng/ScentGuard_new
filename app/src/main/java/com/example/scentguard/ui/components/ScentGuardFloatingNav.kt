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
import com.example.scentguard.data.model.User
import com.example.scentguard.navigation.Screen

@Composable
fun ScentGuardFloatingNav(
    user: User?,
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    // Fill max size to allow alignment to bottom center of the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 16.dp), // Added margin from the bottom of the screen
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(72.dp)
                .widthIn(max = 420.dp)
                .padding(horizontal = 16.dp),
            // Glassmorphism: Semi-transparent surface with high tonal elevation
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = CircleShape,
            tonalElevation = 12.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    label = "Home",
                    screen = Screen.Dashboard,
                    icon = Icons.Outlined.Dashboard,
                    selected = currentRoute == Screen.Dashboard.route,
                    onNavigate = onNavigate
                )
                
                if (user?.role == "Manager") {
                    NavItem(
                        label = "History",
                        screen = Screen.History,
                        icon = Icons.Outlined.History,
                        selected = currentRoute == Screen.History.route,
                        onNavigate = onNavigate
                    )
                }

                NavItem(
                    label = "Alerts",
                    screen = Screen.Notifications,
                    icon = Icons.Outlined.Notifications,
                    selected = currentRoute == Screen.Notifications.route,
                    onNavigate = onNavigate
                )
                NavItem(
                    label = "Profile",
                    screen = Screen.Profile,
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
    screen: Screen,
    icon: ImageVector,
    selected: Boolean,
    onNavigate: (Screen) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content"
    )
    val iconSize by animateDpAsState(
        targetValue = if (selected) 28.dp else 24.dp,
        label = "size"
    )

    Box(
        modifier = Modifier
            .height(56.dp)
            .width(64.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onNavigate(screen) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(iconSize),
                tint = contentColor
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(contentColor, CircleShape)
                )
            }
        }
    }
}
