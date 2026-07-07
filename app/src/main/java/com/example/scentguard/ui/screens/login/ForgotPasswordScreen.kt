package com.example.scentguard.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.ui.components.ScentGuardBackground
import com.example.scentguard.ui.components.ScentGuardButton
import com.example.scentguard.ui.components.ScentGuardCard
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val resetState by viewModel.resetState.collectAsState()

    ScentGuardBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reset Password") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No worries!",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Enter your email and we'll send you a link to reset your password.",
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ScentGuardButton(
                        text = "Send Reset Link",
                        onClick = { viewModel.resetPassword(email) },
                        isLoading = resetState is Resource.Loading
                    )
                    
                    if (resetState is Resource.Success) {
                        Text(
                            text = "Reset email sent! Check your inbox.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    
                    if (resetState is Resource.Error) {
                        Text(
                            text = resetState.message ?: "Error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
