package com.example.scentguard.ui.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.scentguard.utils.responsiveContainer
import com.example.scentguard.viewmodel.MainViewModel
import com.example.scentguard.viewmodel.RegistrationViewModel
import com.example.scentguard.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    viewModel: RegistrationViewModel = viewModel(factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Manager, 1: Staff
    
    var fullName by remember { mutableStateOf("") }
    var restaurantInput by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    
    val registrationState by viewModel.registrationState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    
    val onboardingCompleted by mainViewModel.onboardingCompleted.collectAsState()
    val userProfile by mainViewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isUserAuth by mainViewModel.isUserAuthenticated.collectAsState()
    val isCompleteProfileMode = isUserAuth

    // Pre-fill email and handle session state
    LaunchedEffect(isCompleteProfileMode, mainViewModel.currentUserEmail) {
        if (isCompleteProfileMode) {
            mainViewModel.currentUserEmail?.let { email = it }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(registrationState) {
        if (registrationState is Resource.Success) {
            mainViewModel.fetchUserProfile()
            snackbarHostState.showSnackbar(
                message = if (isCompleteProfileMode) "Profile completed!" else "Success!",
                duration = SnackbarDuration.Short
            )
        }
        
        if (registrationState is Resource.Error) {
            snackbarHostState.showSnackbar(
                message = registrationState.message ?: "Failed",
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(registrationState, onboardingCompleted, userProfile) {
        if (registrationState is Resource.Success && onboardingCompleted != null && userProfile is Resource.Success) {
            delay(500)
            val destination = if (onboardingCompleted == true) Screen.Dashboard.route else Screen.Onboarding.route
            navController.navigate(destination) {
                popUpTo(Screen.SignUp.route) { inclusive = true }
            }
        }
    }

    ScentGuardBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .responsiveContainer(maxWidth = 440.dp)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Image(
                        painter = painterResource(id = R.drawable.ic_scentguard_logo_vector),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = if (isCompleteProfileMode) "Finalize profile" else "Register",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (isCompleteProfileMode) "Finish setting up your workspace" else "Join the ScentGuard network",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        divider = {},
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Manager") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Staff") }
                        )
                    }

                    ScentGuardCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 24.dp,
                        maxWidth = 440.dp
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = restaurantInput,
                            onValueChange = { restaurantInput = it },
                            label = { 
                                Text(if (selectedTab == 0) "Restaurant name" else "Invitation code") 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            ),
                            placeholder = {
                                if (selectedTab == 1) Text("Enter 6-digit code")
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            readOnly = isCompleteProfileMode,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                autoCorrectEnabled = false
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        )
                        
                        if (isCompleteProfileMode) {
                            TextButton(
                                onClick = { 
                                    mainViewModel.logout()
                                    email = ""
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Not you? Sign out", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        
                        if (!isCompleteProfileMode) {
                            Spacer(modifier = Modifier.height(20.dp))
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
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (registrationState is Resource.Loading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Text(
                                    text = statusMessage,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        ScentGuardButton(
                            text = if (isCompleteProfileMode) "Finalize Setup" else if (selectedTab == 0) "Create Restaurant" else "Join Restaurant",
                            onClick = { 
                                val role = if (selectedTab == 0) "Manager" else "Staff"
                                viewModel.register(fullName, restaurantInput, email, role, password, confirmPassword) 
                            },
                            isLoading = registrationState is Resource.Loading,
                            maxWidth = 440.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Have an account? ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text(
                                text = "Sign in",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
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
