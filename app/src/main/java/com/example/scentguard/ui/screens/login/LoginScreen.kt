package com.example.scentguard.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardBackground
import com.example.scentguard.ui.components.ScentGuardButton
import com.example.scentguard.ui.components.ScentGuardCard
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is Resource.Success) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    ScentGuardBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign in to continue monitoring",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            ScentGuardCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = { navController.navigate(Screen.ForgotPassword.route) }) {
                        Text("Forgot Password?", style = MaterialTheme.typography.labelLarge)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ScentGuardButton(
                    text = "Login",
                    onClick = { viewModel.login(email, password) },
                    isLoading = loginState is Resource.Loading
                )
                
                if (loginState is Resource.Error) {
                    Text(
                        text = loginState.message ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { navController.navigate(Screen.SignUp.route) }) {
                Text(
                    text = "Don't have an account? Sign Up",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
