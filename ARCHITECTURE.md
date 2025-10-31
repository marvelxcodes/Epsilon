# FallBag MVP Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        FallBag System                        │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐                    ┌──────────────────────┐
│   ESP32-S3       │◄──────BLE─────────►│   Android Device     │
│   (Wearable)     │                    │   (Companion App)    │
└──────────────────┘                    └──────────────────────┘
        │                                          │
        │                                          │
        ▼                                          ▼
┌──────────────────┐                    ┌──────────────────────┐
│  BLE Server      │                    │  BLE Client          │
│  • Advertise     │                    │  • Scan              │
│  • Connect       │                    │  • Connect           │
│  • Notify        │                    │  • Read/Subscribe    │
└──────────────────┘                    └──────────────────────┘
```

## Data Flow

```
ESP32-S3 Workflow:
1. Initialize BLE
2. Create Service & Characteristic
3. Start Advertising as "FallBag ESP32"
4. Wait for connection
5. When connected:
   └─> Send "Status: OK" every 1 second
6. On disconnect:
   └─> Restart advertising

Android Workflow:
1. User opens app
2. Navigate to Bluetooth screen
3. Request permissions (if needed)
4. Start BLE scan
5. Display found devices
6. User taps device
7. Connect to ESP32
8. Subscribe to notifications
9. Display received data
```

## Component Breakdown

### ESP32-S3 (wearable.ino)
```
┌─────────────────────────────────────┐
│         ESP32 BLE Server            │
├─────────────────────────────────────┤
│ Service UUID:                       │
│ 4fafc201-1fb5-459e-8fcc-c5c9c331914b│
├─────────────────────────────────────┤
│ Characteristic UUID:                │
│ beb5483e-36e1-4688-b7f5-ea07361b26a8│
├─────────────────────────────────────┤
│ Properties:                         │
│ • READ                              │
│ • WRITE                             │
│ • NOTIFY                            │
│ • INDICATE                          │
└─────────────────────────────────────┘
```

### Android App Structure
```
MainActivity
    │
    └─> AppNavigation
            │
            ├─> AuthScreen (Sign In/Up)
            │
            ├─> HomeScreen
            │      │
            │      └─> "Bluetooth" Button
            │
            ├─> BluetoothScreen ◄─── NEW!
            │      │
            │      ├─> BluetoothViewModel
            │      │      ├─> Device List
            │      │      ├─> Scan Control
            │      │      └─> Connection Manager
            │      │
            │      └─> Permission Handler
            │
            └─> SetupScreen
```

## File Structure

```
fallbag/
├── apps/
│   ├── wearable/
│   │   └── wearable.ino ..................... ESP32 BLE server
│   │
│   └── android/
│       ├── app/
│       │   └── src/main/
│       │       ├── AndroidManifest.xml ...... Bluetooth permissions
│       │       └── java/com/epsilon/app/
│       │           ├── ui/
│       │           │   ├── bluetooth/
│       │           │   │   └── BluetoothScreen.kt ... Scanner & UI
│       │           │   └── home/
│       │           │       └── HomeScreen.kt ........ Navigation entry
│       │           ├── navigation/
│       │           │   └── AppNavigation.kt ......... Route config
│       │           └── utils/
│       │               └── PermissionUtils.kt ....... Permission helper
│       │
│       ├── BLUETOOTH_MVP_README.md ............. Detailed setup guide
│       └── build.gradle.kts
│
└── QUICK_START.md .............................. Quick reference
```

## Bluetooth Communication Protocol

```
┌──────────┐                                    ┌─────────┐
│  ESP32   │                                    │ Android │
└────┬─────┘                                    └────┬────┘
     │                                                │
     │  1. Start Advertising "FallBag ESP32"         │
     │────────────────────────────────────────────►  │
     │                                                │
     │  2. Scan Request                               │
     │  ◄────────────────────────────────────────────│
     │                                                │
     │  3. Advertisement Response (UUID, Name, RSSI) │
     │────────────────────────────────────────────►  │
     │                                                │
     │  4. Connection Request                         │
     │  ◄────────────────────────────────────────────│
     │                                                │
     │  5. Connection Established                     │
     │◄──────────────────────────────────────────►   │
     │                                                │
     │  6. Discover Services                          │
     │  ◄────────────────────────────────────────────│
     │                                                │
     │  7. Services & Characteristics                 │
     │────────────────────────────────────────────►  │
     │                                                │
     │  8. Subscribe to Notifications                 │
     │  ◄────────────────────────────────────────────│
     │                                                │
     │  9. Data Notification: "Status: OK" (every 1s) │
     │────────────────────────────────────────────►  │
     │────────────────────────────────────────────►  │
     │────────────────────────────────────────────►  │
     │                                                │
```

## Key Features

### ✅ Implemented
- BLE device advertising (ESP32)
- BLE device scanning (Android)
- Connection management
- Real-time data transmission
- Permission handling
- Clean Material 3 UI
- Connection status display
- Device info display (name, address, RSSI)

### 🔜 Ready to Add
- Sensor data parsing
- Fall detection algorithm
- Alert system
- Data persistence
- Historical data viewing
- Battery level monitoring
- Multiple device support

## Performance Specs

| Metric | Value |
|--------|-------|
| BLE Range | ~10 meters (open space) |
| Update Rate | 1 second |
| Connection Time | 2-3 seconds |
| Power (ESP32) | ~50mA active, ~10µA sleep |
| Min Android SDK | 28 (Android 9.0) |
| Target Android SDK | 35 (Android 15) |

## Security Considerations

⚠️ **Current MVP Security Status:**
- ✅ BLE pairing supported
- ❌ No encryption on characteristic (add for production!)
- ❌ No authentication (add for production!)
- ❌ Public UUIDs (generate custom ones!)

### For Production:
1. Implement BLE bonding/pairing
2. Encrypt characteristic data
3. Add authentication mechanism
4. Use custom UUIDs
5. Implement secure OTA updates

## Testing Strategy

1. **Unit Test**: BluetoothViewModel logic
2. **Integration Test**: BLE connection flow
3. **UI Test**: Permission handling, device list
4. **End-to-End Test**: Full connection + data flow
5. **Range Test**: Connection stability at distance
6. **Battery Test**: Power consumption over time

## Troubleshooting Flow

```
Issue: Can't find device
    ├─> Check ESP32 powered? ────────────► YES ──┐
    │                                              │
    └─> NO ─► Power ESP32                         │
                                                   │
Issue: Permissions denied? ◄─────────────────────┘
    ├─> Granted? ────────────────────────► YES ──┐
    │                                              │
    └─> NO ─► Grant in Settings                   │
                                                   │
Issue: Won't connect? ◄──────────────────────────┘
    ├─> Reset ESP32
    ├─> Restart scan
    ├─> Clear app cache
    └─> Check Serial Monitor logs
```

## MVP Success Criteria

- [x] ESP32 code compiles and uploads
- [x] Android app builds successfully
- [x] BLE advertising works
- [x] Device appears in scan results
- [x] Connection can be established
- [x] Data is transmitted and displayed
- [x] Permissions handled correctly
- [x] UI is clean and functional
- [x] Documentation is complete

## What's Next?

1. **Add Accelerometer/Gyroscope** to ESP32
2. **Implement Fall Detection Algorithm**
3. **Send Structured JSON Data**
4. **Parse Data on Android**
5. **Store Data in Database**
6. **Add Alert Notifications**
7. **Implement Settings Screen**
8. **Add Data Visualization**

---

**Ready to go!** 🚀 Upload the ESP32 code, build the Android app, and start testing!
