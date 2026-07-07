package com.example.scentguard.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardBackground
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha"
    )

    val userProfile by mainViewModel.userProfile.collectAsState()

    LaunchedEffect(key1 = userProfile) {
        if (!startAnimation) {
            startAnimation = true
            delay(2000)
        }
        
        if (userProfile !is Resource.Idle && userProfile !is Resource.Loading) {
            when (userProfile) {
                is Resource.Success -> {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                else -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
        }
    }

    ScentGuardBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .alpha(alphaAnim.value)
            ) {
                Text(
                    text = "ScentGuard",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-1).sp
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "VENT",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Smart Air Quality Monitoring\nfor Restaurant Waste Storage",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Surface(
    color: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Surface(
        color = color,
        shape = shape,
        modifier = modifier
    ) {
        content()
    }
}
