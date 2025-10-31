# Firebase Cloud Messaging (FCM) Integration

This document describes the Firebase Cloud Messaging implementation for triggering emergency calls from the web app to the Android app, even when the app is closed.

## Overview

The system uses Firebase Cloud Messaging (FCM) to send high-priority data messages from the web API to the Android app. These messages trigger emergency calls even when the app is in the background or completely closed.

## Architecture

### Web API (Next.js)

**Files:**
- `/apps/web/src/lib/fcm.ts` - FCM utility functions
- `/apps/web/src/app/api/report/route.ts` - Emergency report endpoint

**Flow:**
1. Web app receives emergency report request
2. Validates user session
3. Sends high-priority FCM message with emergency data
4. FCM delivers message to Android device

### Android App

**Files:**
- `/apps/android/app/src/main/java/com/epsilon/app/service/MyFirebaseMessagingService.kt` - FCM message handler
- `/apps/android/app/src/main/java/com/epsilon/app/service/EmergencyCallReceiver.kt` - Emergency call trigger
- `/apps/android/app/src/main/java/com/epsilon/app/data/fcm/FcmTokenManager.kt` - Token management
- `/apps/android/app/src/main/java/com/epsilon/app/data/fcm/FcmTokenSync.kt` - Backend sync

**Flow:**
1. MyFirebaseMessagingService receives FCM message (even when app is closed)
2. Parses emergency data from message payload
3. Broadcasts emergency call intent
4. EmergencyCallReceiver triggers emergency call using EmergencyCallManager

## Setup Instructions

### Firebase Console Setup

1. **Create Firebase Project:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use existing one
   - Add Android app with package name: `com.epsilon.app`

2. **Download Configuration Files:**
   - Download `google-services.json` from Firebase Console
   - Place it in `/apps/android/app/google-services.json`

3. **Get Service Account Key:**
   - Go to Project Settings → Service Accounts
   - Click "Generate New Private Key"
   - Download the JSON file (keep it secure!)

### Web App Configuration

1. **Set Environment Variable:**
   ```bash
   # In /apps/web/.env
   FIREBASE_SERVICE_ACCOUNT_KEY='{"type":"service_account","project_id":"...","private_key":"..."}'
   ```
   
   The value should be the entire contents of the service account JSON file as a string.

2. **Install Dependencies:**
   ```bash
   cd /e/Projects/fallbag/apps/web
   bun install
   ```

### Android App Configuration

1. **Place google-services.json:**
   ```bash
   # Copy from Firebase Console to:
   /e/Projects/fallbag/apps/android/app/google-services.json
   ```

2. **Sync Gradle:**
   ```bash
   cd /e/Projects/fallbag/apps/android
   ./gradlew --refresh-dependencies
   ```

## Usage

### Sending Emergency Notification from Web API

```typescript
// POST /api/report
{
  "fcmToken": "user-device-fcm-token",
  "reportId": "optional-report-id",
  "location": {
    "latitude": 37.7749,
    "longitude": -122.4194
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Emergency call triggered successfully"
}
```

### Android FCM Token Management

**Getting FCM Token:**
```kotlin
val tokenManager = FcmTokenManager(context)
val token = tokenManager.getToken() // Suspend function
```

**Syncing Token with Backend:**
```kotlin
val fcmSync = FcmTokenSync(context, sessionManager)
fcmSync.syncIfNeeded() // Checks and syncs if needed
```

## How It Works When App is Closed

### Android Side

1. **FCM Wake-up:**
   - FCM uses Google Play Services to deliver messages
   - Even when app is closed, FCM can wake up the app
   - High-priority messages bypass Doze mode restrictions

2. **Background Execution:**
   - `MyFirebaseMessagingService.onMessageReceived()` is called
   - Runs in background thread with ~10 seconds execution time
   - Must complete quickly or schedule work via WorkManager

3. **Emergency Call Trigger:**
   - Service broadcasts emergency intent
   - `EmergencyCallReceiver` receives broadcast
   - Directly calls `EmergencyCallManager.placeEmergencyCall()`
   - Call is placed using `ACTION_CALL` intent

### Important Considerations

1. **Permissions Required:**
   - `CALL_PHONE` - Required to place calls directly
   - `POST_NOTIFICATIONS` (Android 13+) - For notification display
   - User must grant these at runtime

2. **Battery Optimization:**
   - FCM messages bypass Doze mode when high priority
   - No need to disable battery optimization
   - Works even on heavily restricted devices

3. **Delivery Guarantees:**
   - FCM provides "at least once" delivery
   - Messages may be delayed if device is offline
   - No delivery if device has no network connection

## Message Payload Structure

### FCM Data Payload

```json
{
  "type": "EMERGENCY_CALL",
  "userId": "user-123",
  "reportId": "report-456",
  "timestamp": "2025-10-31T12:34:56.789Z",
  "latitude": "37.7749",
  "longitude": "-122.4194"
}
```

**Fields:**
- `type` - Message type identifier (must be "EMERGENCY_CALL")
- `userId` - User ID from authentication
- `reportId` - Optional report/incident ID
- `timestamp` - ISO 8601 timestamp
- `latitude` - Optional location latitude (as string)
- `longitude` - Optional location longitude (as string)

## Testing

### Test FCM from Web API

```bash
curl -X POST http://localhost:3001/api/report \
  -H "Authorization: Bearer YOUR_AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "DEVICE_FCM_TOKEN",
    "reportId": "test-123",
    "location": {
      "latitude": 37.7749,
      "longitude": -122.4194
    }
  }'
```

### Get FCM Token from Android Device

1. Run the app with logging enabled
2. Check logcat for "FCM token" messages:
   ```bash
   adb logcat | grep "FCM"
   ```
3. Token will be printed when app starts or new token is generated

### Manual FCM Test

Use Firebase Console:
1. Go to Cloud Messaging → Send test message
2. Add FCM token
3. Send data payload with emergency structure

## Troubleshooting

### Message Not Received on Android

1. **Check google-services.json:**
   - Verify file exists in `/apps/android/app/`
   - Verify package name matches: `com.epsilon.app`

2. **Check Logcat:**
   ```bash
   adb logcat | grep -E "FCM|Firebase"
   ```

3. **Verify Token:**
   - Token should be 163+ characters
   - Starts with prefix like "cXYZ..."

### Emergency Call Not Triggering

1. **Check Permissions:**
   - Verify `CALL_PHONE` permission granted
   - Check Settings → Apps → FallBag → Permissions

2. **Check Emergency Contact:**
   - User must have emergency contact configured
   - Verify in app settings

3. **Check Logs:**
   ```bash
   adb logcat | grep -E "EmergencyCall|FCMService"
   ```

### Web API Errors

1. **Check Service Account Key:**
   - Verify JSON is valid
   - Verify all required fields present

2. **Check Logs:**
   ```bash
   # In web app directory
   npm run dev
   # Watch console output
   ```

## Security Considerations

1. **Service Account Key:**
   - Never commit to version control
   - Store in environment variables only
   - Rotate periodically

2. **FCM Tokens:**
   - Tokens can be revoked/regenerated
   - Implement token refresh handling
   - Don't expose tokens in logs

3. **Authorization:**
   - Always verify user session before sending FCM
   - Validate user owns the device token
   - Rate limit emergency triggers

## Backend API Requirements

To complete the integration, implement this endpoint:

```typescript
// POST /api/user/fcm-token
// Update user's FCM token in database

interface TokenUpdateRequest {
  fcmToken: string;
  deviceType: "android" | "ios" | "web";
}

// Store in database linked to user
// Used by /api/report to lookup user's device token
```

## Future Enhancements

1. **Multiple Devices:**
   - Support multiple devices per user
   - Send to all devices simultaneously

2. **Delivery Confirmation:**
   - Track message delivery status
   - Retry failed deliveries

3. **Rich Notifications:**
   - Show notification with cancel option
   - Add countdown before call

4. **iOS Support:**
   - Implement APNS for iOS devices
   - Use same backend API structure

## References

- [Firebase Cloud Messaging - Android](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Firebase Admin SDK - Node.js](https://firebase.google.com/docs/admin/setup)
- [Background Message Handling](https://firebase.google.com/docs/cloud-messaging/android/receive)
