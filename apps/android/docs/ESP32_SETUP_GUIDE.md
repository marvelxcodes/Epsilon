# ESP32-S3 Device Setup Guide

## Overview
This guide explains how to set up your ESP32-S3 device to work with the Epsilon authentication app. The device will create a WiFi Access Point that the app can connect to and configure with authentication token and WiFi credentials.

## Architecture

### Provisioning Flow
```
1. Mobile App → Scan for ESP32 AP (SSID: ESP32_Setup_*)
2. Mobile App → Display found devices to user
3. User selects device and enters WiFi credentials
4. Mobile App → Connect to ESP32 AP
5. Mobile App → HTTP POST to ESP32: {token, wifiSSID, wifiPassword}
6. ESP32 → Save configuration to NVS
7. ESP32 → Switch from AP mode to Station mode
8. ESP32 → Connect to user's WiFi network
9. ESP32 → Ready to communicate with backend server
```

### Communication Protocol
1. **ESP32-S3** starts in Access Point (AP) mode on boot
2. **Mobile App** scans and detects ESP32's WiFi network
3. **Mobile App** collects user's WiFi credentials (SSID & password)
4. **Mobile App** connects to ESP32's AP
5. **Mobile App** sends authentication token + WiFi credentials via HTTP POST
6. **ESP32-S3** receives, validates, and stores all configuration
7. **ESP32-S3** switches to Station (STA) mode and connects to user's WiFi
8. **ESP32-S3** is now on the same network as other devices

### JSON Payload Format
```json
{
  "token": "user_authentication_token_here",
  "wifiSSID": "Home_WiFi_Network",
  "wifiPassword": "wifi_password_here"
}
```

## ESP32-S3 Firmware Implementation

### Required Libraries
```cpp
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <ArduinoJson.h>
```

### Complete Arduino Code

```cpp
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <ArduinoJson.h>

// Access Point Configuration
const char* ap_ssid = "ESP32_Setup";      // Change this for your device
const char* ap_password = "";             // Empty for open network
const IPAddress local_ip(192, 168, 4, 1);
const IPAddress gateway(192, 168, 4, 1);
const IPAddress subnet(255, 255, 255, 0);

// Web Server
WebServer server(80);

// Preferences for storing token
Preferences preferences;

// Device ID (can be MAC address or custom ID)
String deviceId;

// Configuration state
bool isConfigured = false;
String authToken = "";

void setup() {
  Serial.begin(115200);
  delay(1000);
  
  // Generate device ID from MAC address
  uint8_t mac[6];
  WiFi.macAddress(mac);
  deviceId = String(mac[0], HEX) + String(mac[1], HEX) + 
             String(mac[2], HEX) + String(mac[3], HEX);
  
  Serial.println("ESP32-S3 Device Setup");
  Serial.println("Device ID: " + deviceId);
  
  // Load saved configuration
  preferences.begin("epsilon", false);
  authToken = preferences.getString("token", "");
  preferences.end();
  
  if (authToken.length() > 0) {
    Serial.println("Device already configured");
    isConfigured = true;
    // You can connect to your WiFi here or do other operations
    connectToWiFi();
  } else {
    Serial.println("Starting Access Point mode...");
    startAccessPoint();
  }
  
  // Setup web server routes
  setupWebServer();
  
  // Start the server
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();
  
  // Add your main application logic here
  if (isConfigured) {
    // Do something with the token
    // e.g., authenticate with backend, control devices, etc.
  }
}

void startAccessPoint() {
  // Stop any existing WiFi connection
  WiFi.disconnect(true);
  delay(100);
  
  // Configure Access Point
  WiFi.mode(WIFI_AP);
  WiFi.softAPConfig(local_ip, gateway, subnet);
  
  bool result = WiFi.softAP(ap_ssid, ap_password);
  
  if (result) {
    Serial.println("Access Point started successfully");
    Serial.println("SSID: " + String(ap_ssid));
    Serial.println("IP Address: " + WiFi.softAPIP().toString());
  } else {
    Serial.println("Failed to start Access Point!");
  }
}

void setupWebServer() {
  // Root endpoint - device information
  server.on("/", HTTP_GET, []() {
    StaticJsonDocument<200> doc;
    doc["deviceId"] = deviceId;
    doc["deviceName"] = "ESP32-S3-" + deviceId;
    doc["status"] = isConfigured ? "configured" : "unconfigured";
    doc["version"] = "1.0.0";
    
    String response;
    serializeJson(doc, response);
    
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", response);
  });
  
  // Configure endpoint - receive authentication token and WiFi credentials
  server.on("/configure", HTTP_POST, []() {
    if (server.hasArg("plain")) {
      String body = server.arg("plain");
      
      StaticJsonDocument<512> doc;
      DeserializationError error = deserializeJson(doc, body);
      
      if (error) {
        Serial.println("JSON parsing failed!");
        server.send(400, "application/json", "{\"error\":\"Invalid JSON\"}");
        return;
      }
      
      // Extract token and WiFi credentials from request
      String token = doc["token"] | "";
      String wifiSSID = doc["wifiSSID"] | "";
      String wifiPassword = doc["wifiPassword"] | "";
      
      if (token.length() == 0) {
        server.send(400, "application/json", "{\"error\":\"Token is required\"}");
        return;
      }
      
      if (wifiSSID.length() == 0 || wifiPassword.length() == 0) {
        server.send(400, "application/json", "{\"error\":\"WiFi credentials are required\"}");
        return;
      }
      
      // Save all configuration to preferences
      preferences.begin("epsilon", false);
      preferences.putString("token", token);
      preferences.putString("wifiSSID", wifiSSID);
      preferences.putString("wifiPassword", wifiPassword);
      preferences.end();
      
      authToken = token;
      isConfigured = true;
      
      Serial.println("Configuration received and saved!");
      Serial.println("Token: " + token);
      Serial.println("WiFi SSID: " + wifiSSID);
      
      // Send success response
      StaticJsonDocument<200> responseDoc;
      responseDoc["success"] = true;
      responseDoc["message"] = "Configuration successful";
      responseDoc["deviceId"] = deviceId;
      
      String response;
      serializeJson(responseDoc, response);
      
      server.sendHeader("Access-Control-Allow-Origin", "*");
      server.send(200, "application/json", response);
      
      // Switch to Station mode and connect to user's WiFi
      delay(2000);
      connectToWiFi(wifiSSID.c_str(), wifiPassword.c_str());
    } else {
      server.send(400, "application/json", "{\"error\":\"No data received\"}");
    }
  });
  
  // Status endpoint - check configuration status
  server.on("/status", HTTP_GET, []() {
    StaticJsonDocument<200> doc;
    doc["configured"] = isConfigured;
    doc["hasToken"] = authToken.length() > 0;
    doc["deviceId"] = deviceId;
    
    String response;
    serializeJson(doc, response);
    
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", response);
  });
  
  // Reset endpoint - clear configuration
  server.on("/reset", HTTP_POST, []() {
    preferences.begin("epsilon", false);
    preferences.clear();
    preferences.end();
    
    authToken = "";
    isConfigured = false;
    
    Serial.println("Configuration reset!");
    
    server.send(200, "application/json", "{\"success\":true,\"message\":\"Device reset\"}");
    
    // Restart in AP mode
    delay(1000);
    ESP.restart();
  });
  
  // Handle CORS preflight
  server.on("/configure", HTTP_OPTIONS, []() {
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.sendHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
    server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
    server.send(204);
  });
}

void connectToWiFi(const char* ssid, const char* password) {
  Serial.println("Switching to Station mode...");
  Serial.println("Connecting to WiFi: " + String(ssid));
  
  // Stop AP mode and switch to Station mode
  WiFi.softAPdisconnect(true);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nConnected to WiFi successfully!");
    Serial.println("IP Address: " + WiFi.localIP().toString());
    
    // Device is now connected to user's WiFi network
    // You can now communicate with your backend server
  } else {
    Serial.println("\nFailed to connect to WiFi");
    Serial.println("Falling back to AP mode...");
    // Fall back to AP mode for reconfiguration
    startAccessPoint();
  }
}
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nConnected to WiFi");
    Serial.println("IP Address: " + WiFi.localIP().toString());
  } else {
    Serial.println("\nFailed to connect to WiFi");
    // Fall back to AP mode
    startAccessPoint();
  }
}
```

## Android App Implementation

### Required Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />
```

### Kotlin Implementation for Token Sending

Create a new file: `app/src/main/java/com/epsilon/app/data/api/ESP32ApiClient.kt`

```kotlin
package com.epsilon.app.data.api

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ConfigureRequest(
    val token: String
)

@Serializable
data class ConfigureResponse(
    val success: Boolean,
    val message: String,
    val deviceId: String? = null
)

class ESP32ApiClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    suspend fun sendToken(token: String): Result<ConfigureResponse> {
        return try {
            val response = client.post("http://192.168.4.1/configure") {
                contentType(ContentType.Application.Json)
                setBody(ConfigureRequest(token = token))
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
    
    suspend fun getDeviceInfo(): Result<Map<String, Any>> {
        return try {
            val response = client.get("http://192.168.4.1/")
            
            if (response.status.isSuccess()) {
                val info: Map<String, Any> = response.body()
                Result.success(info)
            } else {
                Result.failure(Exception("Failed to get device info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        client.close()
    }
}
```

## Testing the Setup

### 1. Upload Firmware to ESP32-S3
1. Open Arduino IDE
2. Install ESP32 board support (v2.0.0 or higher)
3. Install required libraries: ArduinoJson
4. Select board: "ESP32S3 Dev Module"
5. Upload the code

### 2. Test with Serial Monitor
```
ESP32-S3 Device Setup
Device ID: a1b2c3d4
Starting Access Point mode...
Access Point started successfully
SSID: ESP32_Setup
IP Address: 192.168.4.1
HTTP server started
```

### 3. Test Endpoints with curl
```bash
# Get device info
curl http://192.168.4.1/

# Send token
curl -X POST http://192.168.4.1/configure \
  -H "Content-Type: application/json" \
  -d '{"token":"your-auth-token-here"}'

# Check status
curl http://192.168.4.1/status
```

## User Experience Flow

1. **User opens app** → Logs in → Goes to Home Screen
2. **User taps "Setup Device"** → Setup screen opens
3. **Instruction screen appears** with:
   - Clear steps
   - Beautiful illustrations
   - "Scan for Devices" button
4. **User taps scan** → App shows scanning animation
5. **Devices list appears** with:
   - Device name (ESP32-S3-xxx)
   - WiFi signal strength indicator
   - Clean card design
6. **User taps device** → Connecting animation shows
7. **Token is sent automatically** → Progress indicator
8. **Success screen appears** with:
   - Checkmark animation
   - "Setup Complete!" message
   - Device name confirmation
   - "Done" button

## Security Considerations

1. **Token Storage**: ESP32 stores token in encrypted preferences
2. **HTTPS**: Consider adding TLS/SSL for production
3. **Token Validation**: ESP32 should validate token with backend
4. **Timeout**: Configuration window should timeout after 15 minutes
5. **Reset Option**: Physical button to reset device configuration

## Troubleshooting

### ESP32 won't create AP
- Check power supply (needs 5V/500mA minimum)
- Verify antenna connection (if using external antenna)
- Check serial monitor for error messages

### App can't find device
- Ensure Location permissions are granted
- Check WiFi is enabled on phone
- ESP32 should be within 10 meters
- Try rescanning

### Token not received
- Check ESP32 is still in AP mode
- Verify IP address is 192.168.4.1
- Check firewall/security apps on phone

## Next Steps

1. **Add WiFi credentials**: Modify ESP32 to also receive WiFi SSID/password
2. **Backend integration**: ESP32 validates token with your backend
3. **OTA updates**: Add over-the-air firmware updates
4. **Status LED**: Add LED indicators for connection status
5. **BLE alternative**: Consider BLE for initial configuration as alternative
