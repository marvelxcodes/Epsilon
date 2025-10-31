# ESP32 Device Pairing Flow - Complete Documentation

## Overview
The FallBag app uses ESP32S3 wearable devices for fall detection. The pairing flow involves Bluetooth connection for initial WiFi provisioning and device registration.

## Complete Flow

### 1. **User Initiates Pairing**
- User taps "Add Device" button on home screen
- Navigates to Bluetooth screen for device discovery

### 2. **Bluetooth Discovery & Connection**
```
Android App → Scans for ESP32S3 devices via Bluetooth
           → Discovers ESP32 advertising as "FallBag-XXXX"
           → Connects to ESP32 via Bluetooth
```

### 3. **WiFi Provisioning**
```
Android App → Sends WiFi SSID
           → Sends WiFi Password
           → ESP32 connects to WiFi network
           → ESP32 confirms connection success
```

### 4. **Authentication Token Transfer**
```
Android App → Retrieves user's auth token from SessionManager
           → Sends auth token to ESP32 via Bluetooth
           → ESP32 stores token for API authentication
```

### 5. **Device Registration**
```
Android App → Generates unique device ID
           → Calls POST /api/device with:
              {
                "deviceName": "ESP32 Wearable",
                "deviceToken": "esp32-unique-id",
                "deviceModel": "ESP32S3",
                "osVersion": "ESP-IDF v5.x",
                "appVersion": "1.0.0"
              }
           → Server creates device record in database
           → Links device to user account
```

### 6. **Completion**
```
Android App → Shows success message
           → ESP32 appears in "Paired Devices" list
           → Device is ready for fall detection
```

## API Routes

### POST /api/device
**Purpose:** Register a new ESP32 device or update existing one

**Request:**
```json
{
  "deviceName": "ESP32 Wearable",
  "deviceToken": "unique-device-identifier",
  "deviceModel": "ESP32S3",
  "osVersion": "ESP-IDF v5.1",
  "appVersion": "1.0.0"
}
```

**Response:**
```json
{
  "device": {
    "id": "uuid",
    "userId": "user-id",
    "deviceName": "ESP32 Wearable",
    "deviceToken": "unique-device-identifier",
    "deviceModel": "ESP32S3",
    "osVersion": "ESP-IDF v5.1",
    "appVersion": "1.0.0",
    "lastActiveAt": "2025-10-30T12:00:00Z",
    "createdAt": "2025-10-30T12:00:00Z",
    "updatedAt": "2025-10-30T12:00:00Z"
  },
  "message": "Device registered successfully"
}
```

### GET /api/device
**Purpose:** Get all devices for authenticated user

**Response:**
```json
{
  "devices": [
    {
      "id": "uuid",
      "deviceName": "ESP32 Wearable",
      "deviceToken": "unique-id",
      "deviceModel": "ESP32S3",
      "lastActiveAt": "2025-10-30T12:00:00Z"
    }
  ]
}
```

### GET /api/device?deviceToken=xxx
**Purpose:** Check if specific device exists

**Response:**
```json
{
  "exists": true,
  "device": { ... }
}
```

### PATCH /api/device
**Purpose:** Update device information

**Request:**
```json
{
  "deviceId": "uuid",
  "deviceName": "My ESP32",
  "osVersion": "ESP-IDF v5.2"
}
```

## Database Schema

### Device Table
```typescript
{
  id: string (UUID)
  userId: string (Foreign Key to user)
  deviceName: string
  deviceToken: string (Unique identifier from ESP32)
  deviceModel: string (e.g., "ESP32S3")
  osVersion: string (e.g., "ESP-IDF v5.1")
  appVersion: string (e.g., "1.0.0")
  lastActiveAt: timestamp
  createdAt: timestamp
  updatedAt: timestamp
}
```

## Android App Structure

### Home Screen Components

#### 1. Quick Actions
- **Add Device**: Navigates to Bluetooth pairing
- **Edit Profile**: Navigates to profile screen
- **Convert**: Placeholder for future feature
- **Ask AI**: Placeholder for future feature

#### 2. Paired Devices Section
- Shows list of connected ESP32 devices
- Displays device status (Connected/Disconnected)
- Shows battery level
- Tap to configure device settings
- Empty state when no devices paired

#### 3. Settings Section
- **Fall Detection Setup**: Configure sensitivity, alerts
- **Emergency Contacts**: Manage contact list

### Key Functions

#### PairedDevicesSection
```kotlin
@Composable
fun PairedDevicesSection(
    onDeviceClick: (String) -> Unit
)
```
- Fetches devices from API (TODO)
- Displays device cards or empty state
- Handles device click for configuration

#### DeviceCard
```kotlin
@Composable
fun DeviceCard(
    device: DeviceInfo,
    onClick: () -> Unit
)
```
- Shows device name, status, battery
- Click navigates to device details

#### SettingsSection
```kotlin
@Composable
fun SettingsSection(
    onDeviceSetupClick: () -> Unit,
    onEmergencyClick: () -> Unit
)
```
- Displays settings action cards
- No device-specific information here

## ESP32 Responsibilities

### 1. Bluetooth Advertising
```c
// Advertise as "FallBag-XXXX" where XXXX is device ID
esp_ble_gap_config_adv_data(&adv_data);
```

### 2. Receive WiFi Credentials
```c
// Listen for WiFi SSID and password via Bluetooth
void receive_wifi_credentials(char* ssid, char* password)
```

### 3. Connect to WiFi
```c
// Connect to provided WiFi network
esp_wifi_connect();
```

### 4. Receive Auth Token
```c
// Store authentication token for API calls
void store_auth_token(char* token)
```

### 5. Send Fall Detection Data
```c
// POST /api/fall-detection with auth token
http_client_post(url, data, auth_token);
```

## Security Considerations

### 1. Bluetooth Security
- Use BLE pairing with PIN/passkey
- Encrypt data transfer over Bluetooth
- Timeout pairing after 5 minutes

### 2. Auth Token Handling
- Never display token in UI
- Store securely in ESP32 flash (encrypted)
- Rotate token on security events

### 3. WiFi Credentials
- Don't log credentials
- Clear from memory after connection
- Use secure storage on ESP32

## User Experience Flow

```
[Home Screen]
     |
     | Tap "Add Device"
     ▼
[Bluetooth Scan]
     |
     | ESP32 Found
     ▼
[Device Selection]
     |
     | Select ESP32
     ▼
[WiFi Input Dialog]
     |
     | Enter WiFi Credentials
     ▼
[Provisioning]
     |
     | Transfer credentials & token
     ▼
[Registration]
     |
     | API call to register device
     ▼
[Success Screen]
     |
     | "Device Paired Successfully"
     ▼
[Home Screen]
     |
     | Device appears in "Paired Devices"
```

## Error Handling

### Bluetooth Connection Failed
```
- Show error: "Failed to connect to device"
- Retry button
- Help link for troubleshooting
```

### WiFi Connection Failed
```
- Show error: "Device couldn't connect to WiFi"
- Allow re-entry of credentials
- Check network requirements
```

### API Registration Failed
```
- Show error: "Failed to register device"
- Retry button
- Device remains in Bluetooth screen
```

### Token Transfer Failed
```
- Show error: "Failed to configure device"
- Retry button
- Re-establish Bluetooth connection
```

## Future Enhancements

### 1. Device Management
- Remove/unpair devices
- Rename devices
- View device logs
- Update device firmware OTA

### 2. Multi-Device Support
- Connect multiple ESP32s
- Primary/secondary device designation
- Sync settings across devices

### 3. Advanced Configuration
- Adjust fall detection sensitivity
- Configure alert delays
- Set quiet hours
- Custom alert messages

### 4. Battery Management
- Low battery notifications
- Battery usage statistics
- Power saving modes

## Testing Checklist

- [ ] Bluetooth scan discovers ESP32
- [ ] Connection establishes successfully
- [ ] WiFi credentials transferred correctly
- [ ] ESP32 connects to WiFi
- [ ] Auth token transferred securely
- [ ] Device registered in database
- [ ] Device appears in paired devices list
- [ ] Device status updates correctly
- [ ] Battery level displayed accurately
- [ ] Device tap navigates to configuration
- [ ] Empty state shows when no devices
- [ ] Error states handled gracefully
- [ ] Multiple devices supported
- [ ] Device removal works correctly

## Troubleshooting

### Device Not Found
1. Ensure ESP32 is powered on
2. Check Bluetooth is enabled on phone
3. ESP32 should be in pairing mode
4. Try restarting ESP32

### Connection Timeout
1. Move phone closer to ESP32
2. Check for Bluetooth interference
3. Restart Bluetooth on phone
4. Reset ESP32

### WiFi Connection Failed
1. Verify WiFi credentials are correct
2. Check WiFi network is 2.4GHz (ESP32 limitation)
3. Ensure WiFi has internet access
4. Check router firewall settings

### Device Not Appearing in List
1. Check internet connection
2. Verify user is logged in
3. Pull to refresh device list
4. Check API server status
5. Verify device registration succeeded

## References

- ESP-IDF Bluetooth Examples: https://github.com/espressif/esp-idf/tree/master/examples/bluetooth
- WiFi Provisioning: https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/provisioning/wifi_provisioning.html
- Next.js API Routes: https://nextjs.org/docs/app/building-your-application/routing/route-handlers
- Drizzle ORM: https://orm.drizzle.team/
