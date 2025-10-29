package com.epsilon.app.data.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WiFiDeviceManager(private val context: Context) {
    
    private val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    private val connectivityManager = context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Scan for available WiFi networks
     * Returns list of ESP32 Access Points (networks starting with "ESP32")
     */
    suspend fun scanForESP32Devices(): Result<List<ESP32Device>> {
        return try {
            // Check permissions
            if (!hasWiFiPermissions()) {
                return Result.failure(SecurityException("WiFi permissions not granted"))
            }
            
            // Enable WiFi if disabled
            if (!wifiManager.isWifiEnabled) {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = true
            }
            
            // Start scan
            val scanSuccess = wifiManager.startScan()
            
            if (!scanSuccess) {
                return Result.failure(Exception("Failed to start WiFi scan"))
            }
            
            // Wait a bit for scan to complete
            kotlinx.coroutines.delay(3000)
            
            // Get scan results
            val scanResults = wifiManager.scanResults
            
            // Filter for ESP32 devices
            val esp32Devices = scanResults
                .filter { it.SSID.startsWith("ESP32", ignoreCase = true) }
                .map { scanResult ->
                    ESP32Device(
                        name = scanResult.SSID.replace("_", " "),
                        ssid = scanResult.SSID,
                        signalStrength = scanResult.level,
                        isConnected = false,
                        bssid = scanResult.BSSID,
                        capabilities = scanResult.capabilities
                    )
                }
                .distinctBy { it.ssid }
            
            Result.success(esp32Devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Connect to ESP32 WiFi Access Point
     * For Android 10+ uses NetworkRequest API
     * For older versions uses WifiConfiguration
     */
    suspend fun connectToESP32(device: ESP32Device): Result<Boolean> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToESP32Modern(device)
            } else {
                connectToESP32Legacy(device)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Modern WiFi connection for Android 10+
     */
    private suspend fun connectToESP32Modern(device: ESP32Device): Result<Boolean> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return suspendCancellableCoroutine { continuation ->
                val specifier = WifiNetworkSpecifier.Builder()
                    .setSsid(device.ssid)
                    .build()
                
                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(specifier)
                    .build()
                
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        connectivityManager.bindProcessToNetwork(network)
                        if (continuation.isActive) {
                            continuation.resume(Result.success(true))
                        }
                    }
                    
                    override fun onUnavailable() {
                        if (continuation.isActive) {
                            continuation.resume(
                                Result.failure(Exception("Network unavailable"))
                            )
                        }
                    }
                }
                
                connectivityManager.requestNetwork(request, callback)
                
                continuation.invokeOnCancellation {
                    connectivityManager.unregisterNetworkCallback(callback)
                }
            }
        }
        return Result.failure(Exception("Android version not supported"))
    }
    
    /**
     * Legacy WiFi connection for Android 9 and below
     */
    @Suppress("DEPRECATION")
    private suspend fun connectToESP32Legacy(device: ESP32Device): Result<Boolean> {
        return try {
            val conf = WifiConfiguration().apply {
                SSID = "\"${device.ssid}\""
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            }
            
            val netId = wifiManager.addNetwork(conf)
            
            if (netId == -1) {
                return Result.failure(Exception("Failed to add network configuration"))
            }
            
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            
            // Wait for connection
            kotlinx.coroutines.delay(5000)
            
            val connectionInfo = wifiManager.connectionInfo
            val isConnected = connectionInfo.ssid.contains(device.ssid)
            
            if (isConnected) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to connect to network"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from current WiFi network
     */
    fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager.bindProcessToNetwork(null)
        } else {
            @Suppress("DEPRECATION")
            wifiManager.disconnect()
        }
    }
    
    /**
     * Check if we have necessary WiFi permissions
     */
    private fun hasWiFiPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        
        return permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == 
                PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get current WiFi connection info
     */
    fun getCurrentConnection(): String? {
        val connectionInfo = wifiManager.connectionInfo
        return connectionInfo.ssid?.replace("\"", "")
    }
}

data class ESP32Device(
    val name: String,
    val ssid: String,
    val signalStrength: Int,
    val isConnected: Boolean,
    val bssid: String = "",
    val capabilities: String = ""
)
