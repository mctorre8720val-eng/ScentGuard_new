package com.example.scentguard.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.data.model.ChartData
import com.example.scentguard.data.model.ReportSummary
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardChart
import com.example.scentguard.ui.components.ScentGuardFloatingNav
import com.example.scentguard.ui.components.ScentGuardNavigationDrawer
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.ReportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModel: ReportViewModel = viewModel()
) {
    val userProfileResource by mainViewModel.userProfile.collectAsState()
    val user = (userProfileResource as? Resource.Success)?.data
    
    val reportState by viewModel.reportState.collectAsState()
    val chartState by viewModel.chartState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ScentGuardNavigationDrawer(
        user = user,
        currentRoute = Screen.Reports.route,
        drawerState = drawerState,
        onNavigate = { route ->
            scope.launch { drawerState.close() }
            navController.navigate(route) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onLogout = {
            scope.launch { drawerState.close() }
            mainViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Analytics",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { 
                                selectedTab = 0 
                                viewModel.fetchDailyReport()
                            },
                            text = { Text("Daily") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { 
                                selectedTab = 1 
                                viewModel.fetchWeeklyReport()
                            },
                            text = { Text("Weekly") }
                        )
                    }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = reportState) {
                            is Resource.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is Resource.Success -> {
                                ReportContent(state.data!!, chartState)
                            }
                            is Resource.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = state.message ?: "Error", color = MaterialTheme.colorScheme.error)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // The Floating Nav
            ScentGuardFloatingNav(
                user = user,
                currentRoute = Screen.Reports.route,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun ReportContent(report: ReportSummary, chartState: Resource<ChartData>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main Score Card
        ScoreCard(report.airQualityScore)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                when (chartState) {
                    is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is Resource.Success -> ScentGuardChart(chartState.data!!)
                    is Resource.Error -> Text("Failed to load chart", color = MaterialTheme.colorScheme.error)
                    else -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Summary (${report.period})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ReportMetricItem("Average Gas", report.avgGasLevel, Icons.Outlined.Cloud, MaterialTheme.colorScheme.primary)
            ReportMetricItem("Fan Runtime", report.totalFanRuntime, Icons.Outlined.Timer, Color(0xFFF57C00))
            ReportMetricItem("Total Alerts", report.alertsCount.toString(), Icons.Outlined.Warning, MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun ScoreCard(score: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Air Quality Score", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(
                    text = if (score > 90) "Excellent" else "Good",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
                Text(text = "$score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun ReportMetricItem(label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}
