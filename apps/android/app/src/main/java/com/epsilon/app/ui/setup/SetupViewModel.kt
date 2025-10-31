package com.epsilon.app.ui.setup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epsilon.app.data.api.ESP32ApiClient
import com.epsilon.app.data.fcm.FcmTokenSync
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.data.wifi.ESP32Device
import com.epsilon.app.data.wifi.WiFiDeviceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SetupUiState {
    data object Idle : SetupUiState()
    data object Scanning : SetupUiState()
    data class DevicesFound(val devices: List<ESP32Device>) : SetupUiState()
    data class EnteringWiFiCredentials(val device: ESP32Device) : SetupUiState()
    data class Connecting(val device: ESP32Device) : SetupUiState()
    data class SendingConfiguration(val device: ESP32Device) : SetupUiState()
    data class Success(val device: ESP32Device) : SetupUiState()
    data class Error(val message: String) : SetupUiState()
}

class SetupViewModel(
    context: Context,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val wifiManager = WiFiDeviceManager(context)
    private val esp32ApiClient = ESP32ApiClient()
    private val fcmTokenSync = FcmTokenSync(context, sessionManager)
    
    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()
    
    private var authToken: String = ""
    
    init {
        viewModelScope.launch {
            try {
                authToken = sessionManager.token.first() ?: ""
            } catch (e: Exception) {
                android.util.Log.e("SetupViewModel", "Error loading auth token", e)
                authToken = ""
            }
        }
    }
    
    fun startScan() {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Scanning
            
            val result = wifiManager.scanForESP32Devices()
            
            result.fold(
                onSuccess = { devices ->
                    if (devices.isEmpty()) {
                        _uiState.value = SetupUiState.Error(
                            "No ESP32 devices found. Make sure your device is powered on and in AP mode."
                        )
                    } else {
                        _uiState.value = SetupUiState.DevicesFound(devices)
                    }
                },
                onFailure = { error ->
                    _uiState.value = SetupUiState.Error(
                        error.message ?: "Failed to scan for devices. Check WiFi permissions."
                    )
                }
            )
        }
    }
    
    fun connectToDevice(device: ESP32Device) {
        viewModelScope.launch {
            // Transition to WiFi credentials input
            _uiState.value = SetupUiState.EnteringWiFiCredentials(device)
        }
    }
    
    fun sendConfiguration(device: ESP32Device, wifiSSID: String, wifiPassword: String) {
        viewModelScope.launch {
            _uiState.value = SetupUiState.Connecting(device)
            
            // Connect to ESP32 WiFi
            val connectionResult = wifiManager.connectToESP32(device)
            
            connectionResult.fold(
                onSuccess = {
                    // Connection successful, now send configuration
                    _uiState.value = SetupUiState.SendingConfiguration(device)
                    sendConfigurationToDevice(device, wifiSSID, wifiPassword)
                },
                onFailure = { error ->
                    _uiState.value = SetupUiState.Error(
                        "Failed to connect to ${device.name}: ${error.message}"
                    )
                }
            )
        }
    }
    
    private suspend fun sendConfigurationToDevice(
        device: ESP32Device,
        wifiSSID: String,
        wifiPassword: String
    ) {
        // Wait a bit for connection to stabilize
        kotlinx.coroutines.delay(1000)
        
        val result = esp32ApiClient.sendConfiguration(authToken, wifiSSID, wifiPassword)
        
        result.fold(
            onSuccess = { response ->
                if (response.success) {
                    _uiState.value = SetupUiState.Success(device)
                    
                    // Register FCM token after successful setup
                    viewModelScope.launch {
                        try {
                            fcmTokenSync.syncIfNeeded()
                            android.util.Log.d("SetupViewModel", "FCM token registration initiated")
                        } catch (e: Exception) {
                            android.util.Log.e("SetupViewModel", "Error registering FCM token", e)
                        }
                    }
                } else {
                    _uiState.value = SetupUiState.Error(
                        response.message ?: "Configuration failed"
                    )
                }
            },
            onFailure = { error ->
                _uiState.value = SetupUiState.Error(
                    "Failed to send configuration: ${error.message ?: "Network error"}"
                )
            }
        )
    }
    
    fun resetToIdle() {
        _uiState.value = SetupUiState.Idle
    }
    
    fun disconnect() {
        wifiManager.disconnect()
    }
    
    override fun onCleared() {
        super.onCleared()
        esp32ApiClient.close()
        fcmTokenSync.close()
    }
}
