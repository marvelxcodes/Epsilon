# FallBag Bluetooth MVP - Setup Guide

This is a minimal viable product (MVP) for connecting your Android app to the ESP32-S3 wearable device via Bluetooth Low Energy (BLE).

## 🚀 Quick Start

### ESP32-S3 Setup

1. **Install Arduino IDE** (if not already installed)
   - Download from: https://www.arduino.cc/en/software

2. **Install ESP32 Board Support**
   - Open Arduino IDE
   - Go to `File > Preferences`
   - Add this URL to "Additional Board Manager URLs":
     ```
     https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
     ```
   - Go to `Tools > Board > Boards Manager`
   - Search for "esp32" and install "esp32 by Espressif Systems"

3. **Upload the Code**
   - Open `apps/wearable/wearable.ino` in Arduino IDE
   - Select your board: `Tools > Board > ESP32 Arduino > ESP32S3 Dev Module`
   - Select the correct COM port: `Tools > Port > [Your ESP32 Port]`
   - Click the Upload button (→)
   - Wait for "Done uploading" message

4. **Verify It's Working**
   - Open Serial Monitor (`Tools > Serial Monitor`)
   - Set baud rate to `115200`
   - You should see: "Starting BLE work!" and "Waiting for a client connection..."
   - The device is now advertising as **"FallBag ESP32"**

### Android App Setup

1. **Build the Android App**
   ```bash
   cd apps/android
   ./gradlew assembleDebug
   ```

2. **Install on Device**
   - Connect your Android device via USB
   - Enable USB Debugging on your Android device
   - Run:
     ```bash
     ./gradlew installDebug
     ```
   - Or open the project in Android Studio and click Run

3. **Using the App**
   - Open the FallBag app
   - Sign in or create an account (if required)
   - From the home screen, tap the **"Bluetooth"** button
   - Tap **"Grant Bluetooth Permissions"** if prompted
   - Accept all permission requests
   - Tap **"Start Scanning"**
   - Your ESP32 device should appear as **"FallBag ESP32"**
   - Tap on the device to connect
   - Once connected, you'll see "Status: OK" messages from the device

## 📱 Features

### Current Features (MVP)
- ✅ BLE device discovery
- ✅ Connect to ESP32-S3 device
- ✅ Real-time status updates
- ✅ Simple, clean UI
- ✅ Permission handling
- ✅ Connection status display

### Coming Soon
- 📊 Fall detection data transmission
- 📍 Location tracking
- 🔔 Alert notifications
- 📈 Data visualization
- 🔋 Battery level monitoring

## 🔧 Troubleshooting

### ESP32 Issues

**Device not showing up in Serial Monitor**
- Check USB cable (must be data cable, not just charging)
- Hold BOOT button while uploading
- Try different USB port

**Compilation errors**
- Make sure ESP32 board support is installed
- Verify board selection: ESP32S3 Dev Module
- Check that all BLE libraries are included (they come with ESP32 core)

### Android Issues

**"Bluetooth permissions required"**
- Grant all requested permissions
- On Android 12+, you need:
  - Bluetooth Scan
  - Bluetooth Connect
  - Location (even though we use `neverForLocation` flag)

**No devices found**
- Make sure ESP32 is powered on and running
- Check that Bluetooth is enabled on your phone
- Try moving closer to the ESP32 (within 10 meters)
- Restart the scan

**Cannot connect to device**
- Make sure only one device is trying to connect
- Reset the ESP32 (press RESET button)
- Clear Bluetooth cache on Android:
  - Settings > Apps > FallBag > Storage > Clear Cache

## 🔐 BLE Configuration

The current setup uses these UUIDs:
- **Service UUID**: `4fafc201-1fb5-459e-8fcc-c5c9c331914b`
- **Characteristic UUID**: `beb5483e-36e1-4688-b7f5-ea07361b26a8`

These are standard UUIDs for testing. In production, you should generate your own custom UUIDs.

## 📊 Data Flow

```
ESP32-S3 → BLE Advertising → Android Scanning
         → Connection Established
         → ESP32 sends "Status: OK" every 1 second
         → Android displays received data
```

## 🛠️ Development Tips

### Testing the Connection
1. Open Android Studio Logcat to see detailed logs
2. Use Serial Monitor to see ESP32 status
3. Both devices should show connection status changes

### Modifying the Data
To send different data from ESP32, edit this line in `wearable.ino`:
```cpp
String sensorData = "Status: OK"; // Change this!
```

Example for sensor data:
```cpp
String sensorData = "Accel:X=" + String(accelX) + ",Y=" + String(accelY);
```

## 📝 Next Steps

1. **Add Sensors**: Connect accelerometer/gyroscope to ESP32
2. **Parse Data**: Update Android app to parse structured data
3. **Implement Fall Detection**: Add algorithm on ESP32 or Android
4. **Add Notifications**: Send alerts when fall is detected
5. **Store Data**: Save readings to database

## 🆘 Support

If you encounter issues:
1. Check that ESP32 is advertising (Serial Monitor)
2. Verify Android permissions are granted
3. Try restarting both devices
4. Check Bluetooth is enabled on Android

## 📄 License

This is an MVP implementation for testing purposes.
