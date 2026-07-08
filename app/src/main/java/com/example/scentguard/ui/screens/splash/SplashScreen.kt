package com.example.scentguard.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.R
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
    var isTimerFinished by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1200),
        label = "scale"
    )

    val userProfile by mainViewModel.userProfile.collectAsState()
    val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Slightly longer to appreciate the new branding
        isTimerFinished = true
    }

    LaunchedEffect(isTimerFinished, userProfile, onboardingCompleted) {
        if (isTimerFinished && onboardingCompleted != null) {
            when (userProfile) {
                is Resource.Success -> {
                    val destination = if (onboardingCompleted == true) Screen.Dashboard.route else Screen.Onboarding.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                is Resource.Error -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    ScentGuardBackground(showBloom = true) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Subtle Air Waves in the background
            Image(
                painter = painterResource(id = R.drawable.ic_abstract_air_waves),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f),
                alignment = Alignment.TopCenter
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // The New Vector Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_scentguard_logo_vector),
                    contentDescription = "ScentGuard Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scaleAnim)
                        .alpha(alphaAnim)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "ScentGuard",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.alpha(alphaAnim)
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .alpha(alphaAnim)
                ) {
                    Text(
                        text = "VENT",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(64.dp))

                Text(
                    text = "Smart Air Quality Monitoring\nfor Restaurant Waste Storage",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .alpha(alphaAnim)
                )
            }
            
            // Version Label at the bottom
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .alpha(alphaAnim * 0.5f)
            )
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
