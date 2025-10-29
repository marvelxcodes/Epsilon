package com.epsilon.app.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epsilon.app.data.api.AuthApiClient
import com.epsilon.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(context: Context) : ViewModel() {
    
    private val apiClient = AuthApiClient()
    private val sessionManager = SessionManager(context)
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            sessionManager.authToken.collect { token ->
                _isLoggedIn.value = !token.isNullOrEmpty()
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        if (!validateInputs(email, password)) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val result = apiClient.signIn(email, password)
            
            result.fold(
                onSuccess = { response ->
                    // Check if response contains error information
                    if (response.error != null || response.message != null) {
                        _uiState.value = AuthUiState.Error(
                            response.message ?: response.error ?: "Sign in failed"
                        )
                        return@fold
                    }
                    
                    // Check if we have valid success response
                    if (response.token != null && response.user != null) {
                        // Save authentication token
                        sessionManager.saveAuthToken(response.token)
                        
                        // Save user information
                        sessionManager.saveUserInfo(
                            userId = response.user.id,
                            name = response.user.name,
                            email = response.user.email,
                            emailVerified = response.user.emailVerified,
                            createdAt = response.user.createdAt,
                            updatedAt = response.user.updatedAt
                        )
                        
                        _uiState.value = AuthUiState.Success("Successfully signed in!")
                        _isLoggedIn.value = true
                    } else {
                        _uiState.value = AuthUiState.Error("Invalid response from server")
                    }
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(
                        error.message ?: "Network error. Please check your connection."
                    )
                }
            )
        }
    }
    
    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Name cannot be empty")
            return
        }
        
        if (!validateInputs(email, password)) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val result = apiClient.signUp(name, email, password)
            
            result.fold(
                onSuccess = { response ->
                    // Check if response contains error information
                    if (response.error != null || response.message != null) {
                        _uiState.value = AuthUiState.Error(
                            response.message ?: response.error ?: "Sign up failed"
                        )
                        return@fold
                    }
                    
                    // Check if we have valid success response
                    if (response.token != null && response.user != null) {
                        // Save authentication token
                        sessionManager.saveAuthToken(response.token)
                        
                        // Save user information
                        sessionManager.saveUserInfo(
                            userId = response.user.id,
                            name = response.user.name,
                            email = response.user.email,
                            emailVerified = response.user.emailVerified,
                            createdAt = response.user.createdAt,
                            updatedAt = response.user.updatedAt
                        )
                        
                        _uiState.value = AuthUiState.Success("Account created successfully!")
                        _isLoggedIn.value = true
                    } else {
                        _uiState.value = AuthUiState.Error("Invalid response from server")
                    }
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(
                        error.message ?: "Network error. Please check your connection."
                    )
                }
            )
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _uiState.value = AuthUiState.Error("Email cannot be empty")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = AuthUiState.Error("Please enter a valid email")
                false
            }
            password.isBlank() -> {
                _uiState.value = AuthUiState.Error("Password cannot be empty")
                false
            }
            password.length < 6 -> {
                _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _isLoggedIn.value = false
            _uiState.value = AuthUiState.Idle
        }
    }
    
    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        apiClient.close()
    }
}
