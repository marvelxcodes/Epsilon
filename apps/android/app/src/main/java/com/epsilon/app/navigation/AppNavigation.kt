package com.epsilon.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.ui.auth.AuthScreen
import com.epsilon.app.ui.auth.AuthViewModel
import com.epsilon.app.ui.home.HomeScreen
import com.epsilon.app.ui.setup.SetupScreen

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Setup : Screen("setup")
}

@Composable
fun AppNavigation(
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                sessionManager = sessionManager,
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.Setup.route)
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
    }
}
