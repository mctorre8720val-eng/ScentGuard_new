package com.example.scentguard.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.scentguard.R
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardBackground
import com.example.scentguard.ui.components.ScentGuardButton
import com.example.scentguard.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val pages = listOf(
        OnboardingPage.Welcome,
        OnboardingPage.Monitoring,
        OnboardingPage.Ventilation
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    ScentGuardBackground(showBloom = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Hero Illustrations (Top)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = pages[page].illustration),
                        contentDescription = null,
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Floating Content Card (Bottom)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page Content
                    AnimatedContent(
                        targetState = pages[pagerState.currentPage],
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "content"
                    ) { page ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = page.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Indicators & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Indicators
                        PagerIndicator(
                            pageCount = pages.size,
                            currentPage = pagerState.currentPage
                        )

                        // Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pagerState.currentPage < pages.size - 1) {
                                TextButton(
                                    onClick = {
                                        viewModel.completeOnboarding()
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                ) {
                                    Text("Skip", color = MaterialTheme.colorScheme.outline)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.height(56.dp).width(100.dp)
                                ) {
                                    Text("Next")
                                }
                            } else {
                                ScentGuardButton(
                                    text = "Get Started",
                                    onClick = {
                                        viewModel.completeOnboarding()
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PagerIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                label = "width"
            )
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

sealed class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: Int
) {
    object Welcome : OnboardingPage(
        title = "Welcome to ScentGuard",
        description = "Your smart companion for a fresher and safer workspace environment.",
        illustration = R.drawable.img_onboarding_welcome
    )
    object Monitoring : OnboardingPage(
        title = "Real-Time Monitoring",
        description = "Advanced gas sensors constantly track air quality levels in your storage rooms.",
        illustration = R.drawable.img_onboarding_monitoring
    )
    object Ventilation : OnboardingPage(
        title = "Smart Ventilation",
        description = "Automatic fan activation ensures clean air is always circulating when needed.",
        illustration = R.drawable.img_onboarding_ventilation
    )
}
