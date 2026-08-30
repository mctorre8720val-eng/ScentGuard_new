package com.example.scentguard.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.scentguard.data.model.MascotAvatar

@Composable
fun ScentGuardMascotAvatar(
    mascot: MascotAvatar,
    modifier: Modifier = Modifier,
    contentDescription: String? = "Mascot Avatar"
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = mascot.resId),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(0.90f),
            contentScale = ContentScale.Fit
        )
    }
}
