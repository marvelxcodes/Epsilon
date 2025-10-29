package com.epsilon.app.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ConfigureRequest(
    val token: String,
    val wifiSSID: String,
    val wifiPassword: String
)

@Serializable
data class ConfigureResponse(
    val success: Boolean,
    val message: String,
    val deviceId: String? = null
)

@Serializable
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val status: String,
    val version: String
)

class ESP32ApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        // Set timeout for ESP32 local network
        engine {
            requestTimeout = 10000 // 10 seconds
        }
    }
    
    /**
     * Send authentication token and WiFi credentials to ESP32 device
     * ESP32 should be accessible at http://192.168.4.1/configure
     */
    suspend fun sendConfiguration(
        token: String,
        wifiSSID: String,
        wifiPassword: String,
        deviceIp: String = "192.168.4.1"
    ): Result<ConfigureResponse> {
        return try {
            val response = client.post("http://$deviceIp/configure") {
                contentType(ContentType.Application.Json)
                setBody(ConfigureRequest(
                    token = token,
                    wifiSSID = wifiSSID,
                    wifiPassword = wifiPassword
                ))
            }
            
            if (response.status.isSuccess()) {
                val configResponse: ConfigureResponse = response.body()
                Result.success(configResponse)
            } else {
                Result.failure(Exception("Failed with status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send authentication token to ESP32 device (legacy method)
     * @deprecated Use sendConfiguration instead
     */
    @Deprecated(
        message = "Use sendConfiguration with WiFi credentials",
        replaceWith = ReplaceWith("sendConfiguration(token, wifiSSID, wifiPassword, deviceIp)")
    )
    suspend fun sendToken(
        token: String, 
        wifiSSID: String = "", 
        wifiPassword: String = "", 
        deviceIp: String = "192.168.4.1"
    ): Result<ConfigureResponse> {
        return try {
            val response = client.post("http://$deviceIp/configure") {
                contentType(ContentType.Application.Json)
                setBody(ConfigureRequest(
                    token = token,
                    wifiSSID = wifiSSID,
                    wifiPassword = wifiPassword
                ))
            }
            
            if (response.status.isSuccess()) {
                val configResponse: ConfigureResponse = response.body()
                Result.success(configResponse)
            } else {
                Result.failure(Exception("Failed with status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get device information from ESP32
     */
    suspend fun getDeviceInfo(deviceIp: String = "192.168.4.1"): Result<DeviceInfo> {
        return try {
            val response = client.get("http://$deviceIp/")
            
            if (response.status.isSuccess()) {
                val info: DeviceInfo = response.body()
                Result.success(info)
            } else {
                Result.failure(Exception("Failed to get device info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check device status
     */
    suspend fun checkStatus(deviceIp: String = "192.168.4.1"): Result<Boolean> {
        return try {
            val response = client.get("http://$deviceIp/status")
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        client.close()
    }
}
