# Firebase Cloud Messaging Quick Setup

## 🚀 Quick Start

### 1. Firebase Console (5 minutes)

1. Go to https://console.firebase.google.com/
2. Create project → Add Android app
3. Package name: `com.epsilon.app`
4. Download `google-services.json` → place in `/apps/android/app/`
5. Project Settings → Service Accounts → Generate Private Key
6. Save the JSON file securely

### 2. Web App Setup (2 minutes)

```bash
cd /e/Projects/fallbag/apps/web

# Create .env file (if not exists)
echo 'FIREBASE_SERVICE_ACCOUNT_KEY={"type":"service_account",...}' >> .env

# Paste entire service account JSON as value
```

### 3. Android Setup (1 minute)

```bash
# Place google-services.json in:
/e/Projects/fallbag/apps/android/app/google-services.json

# Rebuild
cd /e/Projects/fallbag/apps/android
./gradlew build
```

### 4. Test

**Get FCM Token from Android:**
```bash
adb logcat | grep "FCM token"
# Copy the token (163+ characters)
```

**Send Test Emergency Call:**
```bash
curl -X POST http://localhost:3001/api/report \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fcmToken": "PASTE_FCM_TOKEN_HERE"}'
```

## 📱 How It Works

```
Web App (/api/report)
    ↓ Sends FCM Message
Firebase Cloud Messaging
    ↓ Delivers (even if app closed)
Android Device
    ↓ MyFirebaseMessagingService
EmergencyCallReceiver
    ↓ Triggers
Emergency Call Placed ☎️
```

## ✅ Checklist

### Before Testing
- [ ] `google-services.json` in `/apps/android/app/`
- [ ] `FIREBASE_SERVICE_ACCOUNT_KEY` in `/apps/web/.env`
- [ ] Android app rebuilt with Firebase dependencies
- [ ] User has emergency contact configured
- [ ] `CALL_PHONE` permission granted in Android app

### Verify Working
- [ ] FCM token generated on Android (check logs)
- [ ] Token synced to backend (after setup complete)
- [ ] Web API can send FCM message (no errors)
- [ ] Android receives message (check logs)
- [ ] Emergency call placed successfully

## 🐛 Quick Debug

**Android not receiving messages?**
```bash
# Check if FCM token exists
adb logcat | grep "FCM token"

# Check if message received
adb logcat | grep "FCMService"

# Check google-services.json
ls -la /e/Projects/fallbag/apps/android/app/google-services.json
```

**Web API errors?**
```bash
# Verify service account JSON is valid
echo $FIREBASE_SERVICE_ACCOUNT_KEY | jq .

# Check Next.js logs
cd /e/Projects/fallbag/apps/web
npm run dev
```

**Call not placing?**
```bash
# Check permissions
adb logcat | grep "CALL_PHONE"

# Check emergency contact
adb logcat | grep "EmergencyCall"
```

## 🔗 Key Files

**Web:**
- `apps/web/src/lib/fcm.ts` - FCM sender
- `apps/web/src/app/api/report/route.ts` - API endpoint

**Android:**
- `apps/android/app/src/main/java/com/epsilon/app/service/MyFirebaseMessagingService.kt`
- `apps/android/app/src/main/java/com/epsilon/app/service/EmergencyCallReceiver.kt`
- `apps/android/app/src/main/java/com/epsilon/app/data/fcm/FcmTokenManager.kt`

## 📚 Full Documentation

See [FCM_IMPLEMENTATION.md](./FCM_IMPLEMENTATION.md) for complete details.
