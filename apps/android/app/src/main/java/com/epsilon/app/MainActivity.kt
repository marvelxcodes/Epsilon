package com.epsilon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.navigation.AppNavigation
import com.epsilon.app.ui.auth.AuthViewModel
import com.epsilon.app.ui.theme.EpsilonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val viewModel = AuthViewModel(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        
        setContent {
            EpsilonTheme {
                AppNavigation(
                    viewModel = viewModel,
                    sessionManager = sessionManager
                )
            }
        }
    }
}