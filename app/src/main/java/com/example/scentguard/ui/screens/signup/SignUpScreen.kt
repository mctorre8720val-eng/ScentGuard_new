package com.example.scentguard.ui.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.scentguard.R
import com.example.scentguard.navigation.Screen
import com.example.scentguard.ui.components.ScentGuardBackground
import com.example.scentguard.ui.components.ScentGuardButton
import com.example.scentguard.ui.components.ScentGuardCard
import com.example.scentguard.utils.Resource
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.RegistrationViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModel: RegistrationViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var restaurantInput by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Staff") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val registrationState by viewModel.registrationState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    
    val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()
    val userProfile by mainViewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isCompleteProfileMode by mainViewModel.isUserAuthenticated.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(registrationState) {
        if (registrationState is Resource.Success) {
            // Force refresh global profile to ensure we are "logged in" with data
            mainViewModel.fetchUserProfile()
            
            snackbarHostState.showSnackbar(
                message = if (isCompleteProfileMode) "Profile completed!" else "Account created successfully!",
                duration = SnackbarDuration.Short
            )
        }
        
        if (registrationState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = registrationState.message ?: "Registration failed",
                duration = SnackbarDuration.Long,
                actionLabel = "Retry"
            )
        }
    }

    // Combined Navigation Logic - Watch for Profile to be ready
    LaunchedEffect(registrationState, onboardingCompleted, userProfile) {
        if (registrationState is Resource.Success && onboardingCompleted != null && userProfile is Resource.Success) {
            delay(500) // Brief pause to allow Snackbar to be seen
            val destination = if (onboardingCompleted == true) Screen.Dashboard.route else Screen.Onboarding.route
            navController.navigate(destination) {
                popUpTo(Screen.SignUp.route) { inclusive = true }
            }
        }
    }

    ScentGuardBackground(showBloom = true) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Detail
                Image(
                    painter = painterResource(id = R.drawable.ic_abstract_air_waves),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .alpha(0.1f),
                    alignment = Alignment.TopCenter
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Image(
                        painter = painterResource(id = R.drawable.ic_scentguard_logo_vector),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = if (isCompleteProfileMode) "Complete Profile" else "Create Account",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = if (isCompleteProfileMode) "Finish setting up your workspace" else "Join ScentGuard for a fresher workspace",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
                    )

                    ScentGuardCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 32.dp,
                        contentPadding = 32.dp
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = restaurantInput,
                            onValueChange = { restaurantInput = it },
                            label = { 
                                Text(if (role == "Manager") "Restaurant Name" else "Invitation Code") 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            placeholder = {
                                if (role == "Staff") Text("Enter 6-digit code")
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !isCompleteProfileMode,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                autoCorrectEnabled = false
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Select your role",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            RadioButton(selected = role == "Manager", onClick = { role = "Manager" })
                            Text("Manager", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = role == "Staff", onClick = { role = "Staff" })
                            Text("Staff", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!isCompleteProfileMode) {
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
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(imageVector = image, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (registrationState is Resource.Loading) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                Text(
                                    text = statusMessage,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ScentGuardButton(
                            text = if (isCompleteProfileMode) "Finalize Setup" else "Create Account",
                            onClick = { 
                                // In complete profile mode, we pass a dummy password since it's not used
                                viewModel.register(
                                    fullName, 
                                    restaurantInput, 
                                    email, 
                                    role, 
                                    if (isCompleteProfileMode) "DUMMY_PWD" else password, 
                                    if (isCompleteProfileMode) "DUMMY_PWD" else confirmPassword
                                ) 
                            },
                            isLoading = registrationState is Resource.Loading
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (!isCompleteProfileMode) {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Already have an account? ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Sign In",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        TextButton(onClick = { 
                            mainViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        }) {
                            Text(
                                text = "Use different account",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
