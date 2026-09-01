package com.example.scentguard.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Onboarding : Screen("onboarding")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object Reports : Screen("reports")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object CriticalAlert : Screen("critical_alert")
}
