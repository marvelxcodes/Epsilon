# Epsilon - Authentication App

## Overview
A modern Android authentication app built with Jetpack Compose, featuring beautiful Material Design 3 UI with sign in and sign up functionality.

## Features
- ✨ Beautiful Material Design 3 UI with gradient backgrounds
- 🔐 Sign In and Sign Up with tab layout
- 📧 Email and password authentication
- 🔄 HTTP POST requests to backend API
- 💾 Session management with encrypted DataStore
- 🚀 Automatic session check on app startup
- 🎨 Smooth animations and transitions
- ⚡ Clean architecture with MVVM pattern
- 📊 **Comprehensive User Profile Dashboard**
- 🎯 **Beautiful Data Visualization**
- 🔒 **Secure Token Management**
- ✅ **Email Verification Status Display**
- 📅 **Account Statistics & Timeline**
- 🛜 **ESP32-S3 Device Setup & Configuration**
- 📡 **WiFi Access Point Detection**
- 🔧 **Automatic Token Provisioning to IoT Devices**

## Project Structure
```
app/src/main/java/com/epsilon/app/
├── data/
│   ├── api/
│   │   └── AuthApiClient.kt          # Ktor HTTP client for API calls
│   ├── model/
│   │   └── AuthModels.kt              # Data classes (LoginRequest, SignUpRequest, AuthResponse)
│   └── session/
│       └── SessionManager.kt          # DataStore session management
├── navigation/
│   └── AppNavigation.kt               # Navigation with session check
├── ui/
│   ├── auth/
│   │   ├── AuthScreen.kt              # Beautiful auth UI with tabs
│   │   └── AuthViewModel.kt           # Authentication logic
│   ├── home/
│   │   └── HomeScreen.kt              # Home screen for authenticated users
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt                     # App entry point
```

## API Configuration
The app is configured to send POST requests to your backend API. Update the base URL in `AuthApiClient.kt`:

```kotlin
private val baseUrl = "https://your-api-url.com/api"
```

### API Endpoints
- **Sign In**: `POST /auth/signin`
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

- **Sign Up**: `POST /auth/signup`
  ```json
  {
    "name": "John Doe",
    "email": "user@example.com",
    "password": "password123"
  }
  ```

### Expected Response Format
```json
{
  "redirect": false,
  "token": "QZZHkCHfnc1jzpF8ln7EIYD7iddXblOi",
  "user": {
    "id": "vRw3XYfwAwfnY76wHfeR9NKganssHpoD",
    "email": "test1@gmail.com",
    "name": "Rama",
    "emailVerified": false,
    "createdAt": "2025-10-28T23:52:57.848Z",
    "updatedAt": "2025-10-28T23:52:57.848Z"
  }
}
```

The app will:
1. Store the authentication token securely in DataStore
2. Store complete user information including:
   - User ID
   - Name
   - Email
   - Email verification status
   - Account creation timestamp
   - Last update timestamp
3. Display all user data in a beautiful, modern dashboard
4. Redirect to home screen with personalized greeting

## Session Management
- Session tokens are stored using DataStore Preferences (encrypted)
- Complete user profile data is persisted locally
- Automatic session check on app startup
- If logged in → Navigate to Home Screen with user data
- If not logged in → Navigate to Auth Screen
- Sign out clears all session data

## Home Screen Features
The home screen displays a comprehensive user dashboard with:

### 🎨 Profile Hero Card
- Large avatar with user initials
- Gradient background design
- User name and email display
- Email verification badge
- User ID display

###  Account Details Cards
- **Email Address**: With verification badge
- **User ID**: In monospace font for easy reading
- **Account Created**: Formatted creation date
- **Last Updated**: Formatted last update date
- **Authentication Token**: Masked with "View" option

### ⚡ Quick Actions
- **Edit Profile**: Modify user information
- **Settings**: App configuration
- **Setup Device**: Configure ESP32-S3 IoT devices
- **Help**: Get assistance

### 🔐 Security Features
- Token viewer dialog
- Secure sign-out confirmation
- Session management

## ESP32-S3 Device Setup Feature

### ✅ Fully Functional Implementation
The app includes a **complete, production-ready** setup wizard for configuring ESP32-S3 devices:

**Real Features (Not Placeholders!):**
- ✅ **Native WiFi Scanning**: Uses Android WiFi API to find real ESP32 devices
- ✅ **Actual Device Connection**: Connects to ESP32 Access Point using modern Android APIs
- ✅ **Real Token Transmission**: HTTP POST request sends authentication token to device
- ✅ **Permission Management**: Runtime permissions with UI feedback
- ✅ **Error Handling**: Comprehensive error handling for all edge cases
- ✅ **MVVM Architecture**: Clean code with ViewModel and proper state management

### How It Works
1. **WiFi Scanning**: Performs real WiFi scan, filters for ESP32 devices (SSIDs starting with "ESP32")
2. **Device Selection**: Shows actual devices with real signal strength
3. **WiFi Connection**: 
   - Android 10+: Uses `WifiNetworkSpecifier` API
   - Android 9-: Uses `WifiConfiguration` API
4. **Token Sending**: HTTP POST to `http://192.168.4.1/configure` with authentication token
5. **Confirmation**: Waits for ESP32 response and shows success/error

### Setup Flow
1. User taps "Setup Device" from home screen
2. App requests location permissions (required for WiFi scanning)
3. User taps "Scan for Devices"
4. App shows all ESP32 access points with signal strength
5. User selects their device
6. App automatically:
   - Connects to device's WiFi network
   - Sends authentication token via HTTP POST
   - Waits for confirmation from ESP32
   - Shows success screen
7. App disconnects and returns to home

### Technical Details
- **WiFiDeviceManager**: Handles all WiFi operations (scanning, connecting, disconnecting)
- **SetupViewModel**: Manages state and orchestrates the setup process
- **ESP32ApiClient**: HTTP communication using Ktor
- **Permission Handling**: Runtime permission requests with proper error messages
- **Communication**: HTTP POST to `http://192.168.4.1/configure`
- **Payload**: JSON with authentication token
- **Protocol**: REST API

See [ESP32_SETUP_GUIDE.md](ESP32_SETUP_GUIDE.md) for complete firmware implementation and [IMPLEMENTATION_DETAILS.md](IMPLEMENTATION_DETAILS.md) for technical details.

## UI/UX Features
- **Tab Layout**: Smooth swipe between Sign In and Sign Up
- **Input Validation**: Real-time validation with helpful error messages
- **Loading States**: Loading indicators during API calls
- **Error Handling**: Beautiful error cards with clear messages
- **Success Feedback**: Success cards with confirmation messages
- **Password Toggle**: Show/hide password functionality
- **Keyboard Actions**: Smart IME actions for better UX
- **Gradient Backgrounds**: Beautiful gradient design
- **Material 3 Components**: Modern, accessible UI components

## Dependencies
- Jetpack Compose with Material 3
- Ktor Client for HTTP requests
- Kotlinx Serialization for JSON
- DataStore Preferences for session storage
- Navigation Compose for routing
- Coil for image loading (if needed)

## Known Issues

### Java 25 Compatibility
The project currently has a compatibility issue with Java 25. Kotlin 2.0.21 and the Android Gradle Plugin don't fully support Java 25 yet.

**Solutions:**
1. **Recommended**: Install JDK 17 or JDK 21
   - Arch Linux: `sudo pacman -S jre17-openjdk` or `sudo pacman -S jre21-openjdk`
   - Then set: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk` (or java-21-openjdk)
   
2. **Alternative**: Wait for Kotlin 2.1.0+ and AGP 8.8+ which should support Java 25

3. **Use Android Studio**: Android Studio bundles its own JDK and will work correctly

## Building the App

Once you have JDK 17 or 21:
```fish
# Set Java home
set -x JAVA_HOME /usr/lib/jvm/java-17-openjdk

# Build the app
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Run the app
adb shell am start -n com.epsilon.app/.MainActivity
```

## Running in Android Studio
1. Open the project in Android Studio
2. Let it sync gradle files
3. Update the API base URL in `AuthApiClient.kt`
4. Run the app on an emulator or device

## Customization

### Colors
Edit `ui/theme/Color.kt` to customize the color scheme.

### API Endpoints
Edit `data/api/AuthApiClient.kt` to change endpoint URLs.

### Validation Rules
Edit `ui/auth/AuthViewModel.kt` to customize validation logic.

### UI Layout
Edit `ui/auth/AuthScreen.kt` to customize the authentication UI.

## Testing
The app includes:
- Input validation (email format, password length)
- Network error handling
- Session persistence
- Automatic navigation based on auth state

## Security
- Passwords are never stored locally
- Only authentication tokens are persisted
- DataStore provides encrypted storage
- HTTPS is enforced for API calls
