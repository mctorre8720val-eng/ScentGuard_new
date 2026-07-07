package com.example.scentguard.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.scentguard.ui.theme.Neutral99

@Composable
fun ScentGuardBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = Neutral99,
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}
