# 🚀 FallBag BLE Quick Reference

## ESP32-S3 Upload (Arduino IDE)

1. Open `wearable.ino`
2. Select Board: `ESP32S3 Dev Module`
3. Select Port: Your COM port
4. Click Upload (→)
5. Open Serial Monitor (115200 baud)
6. Device advertises as: **"FallBag ESP32"**

## Android App Usage

### Home Screen → Bluetooth Button → Scan → Connect

```
📱 App Flow:
├── Sign In/Sign Up
├── Home Screen
│   └── "Bluetooth" Quick Action
│       ├── Grant Permissions
│       ├── Start Scanning
│       ├── Device List appears
│       └── Tap device to connect
└── Connected! (Shows status messages)
```

## Key Files Modified/Created

### ESP32
- `apps/wearable/wearable.ino` - BLE server implementation

### Android
- `app/src/main/AndroidManifest.xml` - Added Bluetooth permissions
- `app/src/main/java/com/epsilon/app/ui/bluetooth/BluetoothScreen.kt` - NEW: BLE scanner UI
- `app/src/main/java/com/epsilon/app/utils/PermissionUtils.kt` - NEW: Permission helper
- `app/src/main/java/com/epsilon/app/navigation/AppNavigation.kt` - Added Bluetooth route
- `app/src/main/java/com/epsilon/app/ui/home/HomeScreen.kt` - Added Bluetooth button

## Permissions Required

Android automatically requests:
- ✅ BLUETOOTH_SCAN (Android 12+)
- ✅ BLUETOOTH_CONNECT (Android 12+)
- ✅ ACCESS_FINE_LOCATION
- ✅ BLUETOOTH (Android 11 and below)
- ✅ BLUETOOTH_ADMIN (Android 11 and below)

## BLE Configuration

```
Service UUID:        4fafc201-1fb5-459e-8fcc-c5c9c331914b
Characteristic UUID: beb5483e-36e1-4688-b7f5-ea07361b26a8
Device Name:         FallBag ESP32
Update Rate:         1 second
```

## Testing Checklist

- [ ] ESP32 uploaded successfully
- [ ] Serial Monitor shows "Waiting for a client..."
- [ ] Android app installed
- [ ] Bluetooth permissions granted
- [ ] ESP32 appears in scan results
- [ ] Connection successful
- [ ] Status messages received

## Common Issues & Fixes

| Problem | Solution |
|---------|----------|
| ESP32 not found | • Check ESP32 is powered on<br>• Verify Serial Monitor shows "Waiting..."<br>• Move phone closer to ESP32 |
| Permission denied | • Go to Settings > Apps > FallBag<br>• Grant all Bluetooth permissions<br>• Restart app |
| Won't connect | • Reset ESP32 (press RESET button)<br>• Stop and restart scan<br>• Clear app data |
| Upload failed | • Hold BOOT button during upload<br>• Check USB cable<br>• Verify correct board selected |

## Build Commands

```bash
# Build Android app
cd apps/android
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Build and install
./gradlew installDebug
```

## What Happens After Connection

1. ESP32 sends "Status: OK" every second
2. Android displays received messages
3. Connection status shown in UI
4. Can disconnect with "Disconnect" button

## Next: Add Your Sensors

In `wearable.ino`, replace:
```cpp
String sensorData = "Status: OK";
```

With your sensor data:
```cpp
String sensorData = "X:" + String(x) + ",Y:" + String(y) + ",Z:" + String(z);
```

Then parse it in Android's `onCharacteristicChanged()` callback!
