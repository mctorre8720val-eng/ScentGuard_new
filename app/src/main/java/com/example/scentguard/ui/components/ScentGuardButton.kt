package com.example.scentguard.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scentguard.utils.pressClickEffect
import com.example.scentguard.utils.responsiveContainer

@Composable
fun ScentGuardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    height: Dp = 56.dp, 
    maxWidth: Dp = 400.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .responsiveContainer(maxWidth)
            .height(height)
            .pressClickEffect(),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(20.dp), // Slightly more formal than full circle
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.12f),
            disabledContentColor = containerColor.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium, // More formal title size
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }
    }
}

@Composable
fun ScentGuardSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    height: Dp = 56.dp, 
    maxWidth: Dp = 400.dp
) {
    ScentGuardButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        height = height,
        maxWidth = maxWidth,
        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.primary
    )
}
