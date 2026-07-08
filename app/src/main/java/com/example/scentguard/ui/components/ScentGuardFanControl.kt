package com.example.scentguard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class FanMode {
    ON, OFF, AUTO
}

@Composable
fun ScentGuardFanControl(
    modifier: Modifier = Modifier,
    initialMode: FanMode = FanMode.AUTO,
    onModeChange: (FanMode) -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(initialMode) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Air, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Ventilation Control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current: ${selectedMode.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FanControlButton(
                    label = "ON",
                    icon = Icons.Outlined.PowerSettingsNew,
                    isSelected = selectedMode == FanMode.ON,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedMode = FanMode.ON
                        onModeChange(FanMode.ON)
                        // TODO: Phase 2 - Send "ON" command to Firebase Realtime Database
                        // TODO: Phase 2 - ESP32 will listen for this change and activate relay
                    }
                )
                FanControlButton(
                    label = "OFF",
                    icon = Icons.Outlined.PowerSettingsNew,
                    isSelected = selectedMode == FanMode.OFF,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedMode = FanMode.OFF
                        onModeChange(FanMode.OFF)
                        // TODO: Phase 2 - Send "OFF" command to Firebase Realtime Database
                        // TODO: Phase 2 - ESP32 will listen for this change and deactivate relay
                    }
                )
                FanControlButton(
                    label = "AUTO",
                    icon = Icons.Outlined.AutoMode,
                    isSelected = selectedMode == FanMode.AUTO,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedMode = FanMode.AUTO
                        onModeChange(FanMode.AUTO)
                        // TODO: Phase 2 - Send "AUTO" command to Firebase Realtime Database
                        // TODO: Phase 2 - ESP32 will handle ventilation logic based on sensor thresholds
                    }
                )
            }
        }
    }
}

@Composable
private fun FanControlButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contentColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
