# Fall Detection Emergency Call System - Quick Start Guide

## 🎯 What Was Implemented

A complete background service system that:
- ✅ Monitors Supabase Realtime for fall detection events
- ✅ Automatically places emergency calls when falls are detected
- ✅ Runs in background even when phone is locked
- ✅ Auto-starts on device boot
- ✅ Survives app closure and system restarts
- ✅ Provides UI for emergency contact configuration
- ✅ Handles all required permissions

## 📁 Files Created

```
apps/android/
├── app/src/main/java/com/epsilon/app/
│   ├── data/
│   │   ├── emergency/
│   │   │   ├── EmergencyCallManager.kt          # Places emergency calls
│   │   │   └── EmergencyContactManager.kt       # Stores emergency contact
│   │   ├── model/
│   │   │   └── Fall.kt                          # Fall data model
│   │   └── supabase/
│   │       └── SupabaseManager.kt               # Supabase client & Realtime
│   ├── receiver/
│   │   └── BootReceiver.kt                      # Auto-start on boot
│   ├── service/
│   │   └── FallDetectionService.kt              # Main background service
│   └── ui/
│       └── emergency/
│           └── EmergencyContactScreen.kt        # Emergency contact UI
├── FALL_DETECTION_SERVICE_README.md             # Detailed documentation
├── SUPABASE_CONFIGURATION.md                    # Setup guide
└── build.gradle.kts                             # Updated with dependencies
```

## 🚀 Implementation Steps

### Step 1: Configure Supabase (5 minutes)

1. **Get Your Supabase Credentials**
   - Go to [Supabase Dashboard](https://app.supabase.com)
   - Navigate to: Settings > API
   - Copy your Project URL and anon key

2. **Update SupabaseManager.kt**
   ```kotlin
   // File: app/src/main/java/com/epsilon/app/data/supabase/SupabaseManager.kt
   // Lines 18-19
   
   private const val SUPABASE_URL = "https://your-project.supabase.co"
   private const val SUPABASE_ANON_KEY = "your-anon-key-here"
   ```

3. **Create Database Table**
   Run in Supabase SQL Editor:
   ```sql
   create table falls (
     id uuid primary key default uuid_generate_v4(),
     user_id uuid references auth.users(id),
     is_fall boolean default false,
     detected_at timestamp default now()
   );

   alter table falls enable row level security;

   create policy "Users can view own falls"
     on falls for select
     using (auth.uid() = user_id);

   create policy "Anyone can insert falls"
     on falls for insert
     with check (true);

   alter publication supabase_realtime add table falls;
   ```

### Step 2: Build the Project (2 minutes)

```bash
cd apps/android
./gradlew build
```

If you see any errors, sync Gradle first in Android Studio:
- File > Sync Project with Gradle Files

### Step 3: Run the App (1 minute)

```bash
./gradlew installDebug
```

Or in Android Studio: Run > Run 'app'

### Step 4: Configure Emergency Contact (3 minutes)

1. **Login to the app** with your user account

2. **Navigate to Emergency Contact**
   - From home screen, tap "Emergency" button
   - Or navigate via your app's menu

3. **Grant Permissions**
   - Tap "Grant Permission" button
   - Allow "Make and manage phone calls"
   - Allow "Post notifications" (Android 13+)

4. **Enter Emergency Contact**
   - Enter phone number (e.g., +1234567890)
   - Optionally enter contact name
   - Tap "Save Emergency Contact"

5. **Verify Service Started**
   - Pull down notification shade
   - Look for "Fall Detection Active" notification
   - If present, service is running ✅

### Step 5: Test the System (5 minutes)

#### Option A: Manual Test via Supabase Dashboard

1. **Get Your User ID**
   - In the app, note your user ID from home screen
   - Or check logs: `adb logcat | grep "user:"`

2. **Insert Test Fall Event**
   - Go to Supabase Dashboard > Table Editor > falls
   - Click "Insert row"
   - Set:
     - user_id: [your-user-id]
     - is_fall: true
     - detected_at: now()
   - Click "Save"

3. **Observe Result**
   - App should immediately place call to emergency contact
   - Check notification updates
   - Check logcat: `adb logcat | grep "FallDetection"`

#### Option B: Test from Wearable

```cpp
// In your wearable code (ESP32/Arduino)
// After detecting a fall:

void reportFall() {
  HTTPClient http;
  http.begin("https://your-project.supabase.co/rest/v1/falls");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("apikey", "your-anon-key");
  http.addHeader("Authorization", "Bearer your-anon-key");
  
  String payload = "{\"user_id\":\"" + userId + "\",\"is_fall\":true}";
  int httpCode = http.POST(payload);
  
  http.end();
}
```

## 🔍 Verification Checklist

- [ ] Supabase credentials configured
- [ ] Database table created with RLS policies
- [ ] Realtime enabled for falls table
- [ ] App builds without errors
- [ ] Emergency contact saved in app
- [ ] CALL_PHONE permission granted
- [ ] "Fall Detection Active" notification visible
- [ ] Test fall event triggers call
- [ ] Service survives app closure
- [ ] Service survives device reboot

## 📱 Usage After Setup

### Normal Operation

Once configured:
1. Service starts automatically when user logs in
2. Service runs in background continuously
3. Service monitors for fall events 24/7
4. On fall detection → automatic emergency call
5. Service restarts on device boot

### User Experience

**When Fall Detected:**
1. Notification updates: "⚠️ Fall detected! Calling emergency contact..."
2. Phone immediately calls saved emergency contact
3. Call connects without user interaction
4. Notification shows call status

**Background Operation:**
- Persistent "Fall Detection Active" notification
- Low battery usage (partial wake lock only)
- Runs even when phone is locked
- Survives task removal from recent apps

## 🛠️ Troubleshooting

### Service Not Starting

**Check logs:**
```bash
adb logcat | grep FallDetectionService
```

**Common causes:**
- User not logged in → Service only runs for logged-in users
- Check: `adb logcat | grep "User logged in"`

### Call Not Placing

**Check permissions:**
```bash
adb shell dumpsys package com.epsilon.app | grep CALL_PHONE
```

**Common causes:**
- CALL_PHONE permission not granted
- Emergency contact not configured
- No telephony capability (tablets)

**Test manually:**
```kotlin
// In app code or adb shell
val emergencyContactManager = EmergencyContactManager(context)
val hasContact = emergencyContactManager.hasEmergencyContact()
Log.d("Test", "Has emergency contact: $hasContact")

val emergencyCallManager = EmergencyCallManager(context)
val hasPermission = emergencyCallManager.hasCallPermission()
Log.d("Test", "Has call permission: $hasPermission")
```

### Realtime Not Receiving Events

**Check Supabase connection:**
```bash
adb logcat | grep SupabaseManager
```

**Look for:**
- "Subscribing to falls for user: [user-id]" ✅
- "Successfully subscribed to falls channel" ✅
- "Fall detected: Fall(...)" ✅

**Common causes:**
- Incorrect SUPABASE_URL or SUPABASE_ANON_KEY
- Realtime not enabled: `alter publication supabase_realtime add table falls;`
- Network connectivity issues
- RLS policy blocking access

**Test Realtime manually:**
```sql
-- In Supabase SQL Editor
-- Insert with your user ID
insert into falls (user_id, is_fall) 
values ('your-user-id-here', true);
```

### Service Stops After Time

**Check battery optimization:**
```bash
adb shell dumpsys deviceidle whitelist | grep com.epsilon.app
```

**Disable battery optimization:**
- Settings > Apps > Epsilon > Battery > Unrestricted

**Manufacturer-specific:**
- **Xiaomi (MIUI):** Security > Manage apps > Epsilon > Autostart: ON
- **Huawei (EMUI):** Settings > Battery > App launch > Epsilon > Manage manually
- **Samsung:** Settings > Apps > Epsilon > Battery > Optimize battery usage > All > Epsilon > Don't optimize
- **OnePlus (OxygenOS):** Settings > Battery > Battery optimization > Epsilon > Don't optimize

## 🔐 Security Best Practices

### For Development
- ✅ Using anon key is fine for development
- ✅ RLS policies protect user data
- ✅ Emergency contact in SharedPreferences (private)

### For Production
- ⚠️ Move credentials to environment variables
- ⚠️ Consider encrypting emergency contact
- ⚠️ Add additional authentication checks
- ⚠️ Implement rate limiting on fall inserts
- ⚠️ Add call logging for audit trail
- ⚠️ Consider adding SMS fallback

## 📊 Monitoring in Production

### Key Metrics to Track
- Service uptime percentage
- Emergency call success rate
- Fall detection events per user
- Average response time (detection → call)
- Permission grant rates
- Service restart frequency

### Logging
All components use Android Log with tags:
- `FallDetectionService` - Service lifecycle
- `SupabaseManager` - Realtime connection
- `EmergencyCallManager` - Call placement
- `BootReceiver` - Auto-start events

View all logs:
```bash
adb logcat | grep -E "FallDetection|SupabaseManager|EmergencyCall|BootReceiver"
```

## 🎓 Architecture Summary

```
┌──────────────────────────────────────────────────────────┐
│                     Wearable Device                       │
│  (ESP32 detects fall → Inserts to Supabase falls table) │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────┐
│                  Supabase Realtime                        │
│        (Broadcasts INSERT event to subscribers)          │
└────────────────────┬─────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────┐
│              FallDetectionService (Android)               │
│  ┌────────────────────────────────────────────────────┐ │
│  │  SupabaseManager.subscribeToFalls()                │ │
│  │    - Filters: user_id=eq.current_user              │ │
│  │    - Filters: is_fall=true                         │ │
│  │    - Receives: Fall record                          │ │
│  └─────────────────┬──────────────────────────────────┘ │
│                    │                                      │
│                    ▼                                      │
│  ┌────────────────────────────────────────────────────┐ │
│  │  handleFallDetected(fall)                          │ │
│  │    1. Get emergency contact                         │ │
│  │    2. Check CALL_PHONE permission                  │ │
│  │    3. Place emergency call                          │ │
│  │    4. Update notification                           │ │
│  └────────────────┬──────────────────────────────────┘ │
└───────────────────┼──────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────────────────┐
│              EmergencyCallManager                         │
│  - TelecomManager.placeCall() (Android 10+)             │
│  - Fallback: ACTION_CALL intent                          │
│  - Result: Phone calls emergency contact                 │
└──────────────────────────────────────────────────────────┘
```

## 📞 Support

If you encounter issues:

1. **Check logs** - Most issues are logged with clear messages
2. **Review README** - Detailed troubleshooting in FALL_DETECTION_SERVICE_README.md
3. **Verify configuration** - Double-check Supabase credentials
4. **Test permissions** - Ensure all required permissions granted
5. **Check device** - Some manufacturers aggressively kill background services

## ✅ Next Steps

1. **Test thoroughly** with various scenarios
2. **Add error handling** for edge cases
3. **Implement analytics** to track usage
4. **Add SMS fallback** if calls fail
5. **Support multiple contacts** for redundancy
6. **Add confirmation dialog** option (configurable)
7. **Include location** in emergency response
8. **Add medical information** sharing
9. **Implement call logs** for audit trail
10. **Set up monitoring** for production

---

**Implementation Status:** ✅ Complete and Ready for Testing

All components have been implemented, integrated, and documented. The system is ready for configuration and testing.
