package com.example.scentguard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scentguard.data.model.MascotAvatar
import com.example.scentguard.data.model.MascotAvatars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    onDismiss: () -> Unit,
    selectedAvatarId: String?,
    onAvatarSelected: (String) -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }
    var pendingMascot by remember { mutableStateOf<MascotAvatar?>(null) }

    if (showConfirmation && pendingMascot != null) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Change Guardian?") },
            text = { Text("Your profile icon will be changed to ${pendingMascot?.name}. This will update how you appear across ScentGuard.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAvatarSelected(pendingMascot!!.id)
                        showConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Choose your Mascot",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Select a guardian that represents your style.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MascotAvatars.collection) { mascot ->
                    MascotTile(
                        mascot = mascot,
                        isSelected = mascot.id == selectedAvatarId,
                        onClick = {
                            if (mascot.id != selectedAvatarId) {
                                pendingMascot = mascot
                                showConfirmation = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MascotTile(
    mascot: MascotAvatar,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.05f else 1f, label = "scale")
    val borderColor = if (isSelected) Color(0xFF34C759) else Color.Transparent
    val borderAlpha = if (isSelected) 1f else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.3f else 0.1f),
        border = BorderStroke(2.dp, borderColor.copy(alpha = borderAlpha))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                ScentGuardMascotAvatar(
                    mascot = mascot,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = mascot.name
                )
                
                if (isSelected) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(24.dp).padding(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color(0xFF34C759),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = mascot.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = mascot.personality,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 1
            )
        }
    }
}
