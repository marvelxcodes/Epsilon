package com.epsilon.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.ui.auth.AuthScreen
import com.epsilon.app.ui.auth.AuthViewModel
import com.epsilon.app.ui.bluetooth.BluetoothScreen
import com.epsilon.app.ui.emergency.EmergencyContactScreen
import com.epsilon.app.ui.home.HomeScreen
import com.epsilon.app.ui.profile.ProfileScreen
import com.epsilon.app.ui.medication.MedicationScreen
import com.epsilon.app.ui.reminder.ReminderScreen
import com.epsilon.app.ui.setup.SetupScreen

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Setup : Screen("setup")
    data object Bluetooth : Screen("bluetooth")
    data object EmergencyContact : Screen("emergency_contact")
    data object Medication : Screen("medication")
    data object Reminder : Screen("reminder")
}

@Composable
fun AppNavigation(
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var isInitialized by remember { mutableStateOf(false) }
    var startRoute by remember { mutableStateOf<String?>(null) }
    
    // Determine the start route based on login state
    LaunchedEffect(Unit) {
        // Wait a moment for the state to initialize
        kotlinx.coroutines.delay(100)
        startRoute = if (isLoggedIn) Screen.Home.route else Screen.Auth.route
        isInitialized = true
    }
    
    // Handle navigation when login state changes (after initialization)
    LaunchedEffect(isLoggedIn) {
        if (!isInitialized) return@LaunchedEffect
        
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        
        when {
            isLoggedIn && currentRoute == Screen.Auth.route -> {
                // User logged in, navigate to home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
            !isLoggedIn && currentRoute != null && currentRoute != Screen.Auth.route -> {
                // User logged out, navigate to auth
                navController.navigate(Screen.Auth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    
    // Show loading while determining initial route
    if (!isInitialized || startRoute == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    NavHost(
        navController = navController,
        startDestination = startRoute!!
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    // Navigation will be handled by LaunchedEffect above
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                sessionManager = sessionManager,
                onSignOut = {
                    // Navigation will be handled by LaunchedEffect above
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.Setup.route)
                },
                onNavigateToBluetooth = {
                    navController.navigate(Screen.Bluetooth.route)
                },
                onNavigateToEmergencyContact = {
                    navController.navigate(Screen.EmergencyContact.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToMedication = {
                    navController.navigate(Screen.Medication.route)
                },
                onNavigateToReminder = {
                    navController.navigate(Screen.Reminder.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                sessionManager = sessionManager,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    // Navigation will be handled by LaunchedEffect above
                }
            )
        }
        
        composable(Screen.Setup.route) {
            SetupScreen(
                sessionManager = sessionManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Bluetooth.route) {
            BluetoothScreen(
                sessionManager = sessionManager,
                onConfigured = { navController.popBackStack() }
            )
        }
        
        composable(Screen.EmergencyContact.route) {
            EmergencyContactScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Medication.route) {
            MedicationScreen(
                sessionManager = sessionManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Reminder.route) {
            ReminderScreen(
                sessionManager = sessionManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
