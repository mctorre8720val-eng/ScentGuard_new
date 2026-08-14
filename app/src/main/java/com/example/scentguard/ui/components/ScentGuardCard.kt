package com.example.scentguard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.scentguard.utils.pressClickEffect

@Composable
fun ScentGuardCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    if (onClick != null) {
        Card(
            modifier = modifier.pressClickEffect(),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            ),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        ) {
            Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                content()
            }
        }
    }
}
