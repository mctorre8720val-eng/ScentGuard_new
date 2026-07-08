package com.example.scentguard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scentguard.ui.screens.dashboard.DashboardScreen
import com.example.scentguard.ui.screens.devices.DevicesScreen
import com.example.scentguard.ui.screens.history.HistoryScreen
import com.example.scentguard.ui.screens.notifications.NotificationsScreen
import com.example.scentguard.ui.screens.settings.SettingsScreen
import com.example.scentguard.ui.screens.login.ForgotPasswordScreen
import com.example.scentguard.ui.screens.login.LoginScreen
import com.example.scentguard.ui.screens.profile.ProfileScreen
import com.example.scentguard.ui.screens.reports.ReportsScreen
import com.example.scentguard.ui.screens.signup.SignUpScreen
import com.example.scentguard.ui.screens.splash.SplashScreen
import com.example.scentguard.viewmodel.MainViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable("devices") {
            DevicesScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.Notifications.route) { 
            NotificationsScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.History.route) { 
            HistoryScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.Reports.route) { 
            ReportsScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.Settings.route) { 
            SettingsScreen(navController = navController, mainViewModel = mainViewModel)
        }
        composable(Screen.Profile.route) { 
            ProfileScreen(navController = navController, mainViewModel = mainViewModel)
        }
    }
}
