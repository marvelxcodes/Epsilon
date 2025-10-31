# Fall Detection Emergency Call Service - Android

## Overview

This implementation provides a comprehensive background service that monitors Supabase Realtime for fall detection events and automatically places emergency calls when a fall is detected.

## Architecture

```
┌─────────────┐          ┌──────────────┐          ┌─────────────┐
│  Wearable   │─ fall ──>│   Supabase   │─ event ─>│   Android   │
│   Device    │  insert  │   Realtime   │  stream  │     App     │
└─────────────┘          └──────────────┘          └─────────────┘
                                                           │
                                                           v
                                                    ┌──────────────┐
                                                    │ Emergency    │
                                                    │ Call Placed  │
                                                    └──────────────┘
```

## Components

### 1. Data Models

**`Fall.kt`**
- Represents fall detection records from Supabase
- Serializable data class for Realtime events

### 2. Core Managers

**`SupabaseManager.kt`**
- Initializes Supabase client with Realtime support
- Subscribes to fall detection events for logged-in user
- Filters events where `is_fall = true`
- Handles real-time streaming with error recovery

**`EmergencyContactManager.kt`**
- Stores emergency contact using SharedPreferences
- Provides CRUD operations for emergency contact
- Validates contact configuration

**`EmergencyCallManager.kt`**
- Places emergency calls using Android's telephony APIs
- Attempts TelecomManager.placeCall() for privileged calls (Android 10+)
- Falls back to ACTION_CALL intent
- Handles permission checks and telephony capabilities

### 3. Service Components

**`FallDetectionService.kt`**
- Foreground service that runs persistently
- Subscribes to Supabase Realtime on start
- Monitors for fall detection events
- Automatically places emergency calls when fall detected
- Keeps device awake using WakeLock
- Displays persistent notification
- Restarts automatically if killed by system

**`BootReceiver.kt`**
- BroadcastReceiver for BOOT_COMPLETED
- Automatically starts service when device boots
- Only starts if user is logged in

### 4. UI Components

**`EmergencyContactScreen.kt`**
- Jetpack Compose UI for emergency contact setup
- Permission request handling
- Validates phone number input
- Shows status messages and warnings

## Setup Instructions

### 1. Configure Supabase Credentials

Edit `SupabaseManager.kt` and replace with your credentials:

```kotlin
private const val SUPABASE_URL = "https://your-project.supabase.co"
private const val SUPABASE_ANON_KEY = "your-anon-key-here"
```

### 2. Create Supabase Table

Run this SQL in your Supabase SQL Editor:

```sql
create table falls (
  id uuid primary key default uuid_generate_v4(),
  user_id uuid references auth.users(id),
  is_fall boolean default false,
  detected_at timestamp default now()
);

-- Enable Row Level Security
alter table falls enable row level security;

-- Policy: Users can only read their own falls
create policy "Users can view own falls"
  on falls for select
  using (auth.uid() = user_id);

-- Policy: Anyone can insert falls (for wearable)
create policy "Anyone can insert falls"
  on falls for insert
  with check (true);

-- Enable Realtime
alter publication supabase_realtime add table falls;
```

### 3. Sync Gradle

The dependencies have been added to `build.gradle.kts`. Sync your project:

```bash
./gradlew build
```

### 4. Add Emergency Contact to Navigation

Add the EmergencyContactScreen to your app's navigation graph. Example:

```kotlin
// In AppNavigation.kt or similar
composable("emergency_contact") {
    EmergencyContactScreen(
        onNavigateBack = { navController.navigateUp() }
    )
}
```

### 5. Request Permissions

The app requires these permissions (already added to AndroidManifest.xml):

- `CALL_PHONE` - Place emergency calls
- `FOREGROUND_SERVICE` - Run persistent service
- `FOREGROUND_SERVICE_PHONE_CALL` - Android 14+ requirement
- `RECEIVE_BOOT_COMPLETED` - Auto-start on boot
- `WAKE_LOCK` - Keep device awake for monitoring
- `POST_NOTIFICATIONS` - Show foreground service notification (Android 13+)

Users will be prompted to grant these permissions through the EmergencyContactScreen.

## Usage Flow

### Initial Setup

1. User logs into the app
2. User navigates to Emergency Contact screen
3. User grants CALL_PHONE permission
4. User enters emergency contact number and saves
5. FallDetectionService starts automatically

### Fall Detection Flow

1. Wearable detects a fall
2. Wearable inserts record: `supabase.from("falls").insert({ user_id, is_fall: true })`
3. Android service receives Realtime event
4. Service validates emergency contact exists
5. Service checks CALL_PHONE permission
6. Service places emergency call to saved contact
7. Service updates notification with status

### Background Operation

- Service runs as foreground service with persistent notification
- Service holds partial wake lock to monitor even when device sleeps
- Service automatically restarts if killed by system (START_STICKY)
- Service automatically starts on device boot if user is logged in
- Service runs even when app is closed or phone is locked

## Testing

### Test Service Manually

```kotlin
// Start service
FallDetectionService.startService(context)

// Stop service  
FallDetectionService.stopService(context)
```

### Test Fall Detection

From your wearable or using Supabase client:

```javascript
// Insert a test fall
const { data, error } = await supabase
  .from('falls')
  .insert({
    user_id: 'your-user-id',
    is_fall: true
  });
```

### Monitor Logs

```bash
adb logcat | grep -E "FallDetection|SupabaseManager|EmergencyCall"
```

## Permissions Handling

### Required Permissions

1. **CALL_PHONE** - Must be granted for automatic calling
2. **FOREGROUND_SERVICE** - Automatically granted (normal permission)
3. **RECEIVE_BOOT_COMPLETED** - Automatically granted (normal permission)
4. **WAKE_LOCK** - Automatically granted (normal permission)

### Privileged Calling (Optional)

For true zero-interaction calling on Android 10+:

1. App must be system app, or
2. App must have ROLE_DIALER, or
3. App must use TelecomManager with proper permissions

The current implementation attempts privileged calling but falls back to ACTION_CALL which may show a brief system UI.

## Battery Optimization

### Disable Battery Optimization

For reliability, users should disable battery optimization for the app:

```kotlin
// Request to ignore battery optimization
val intent = Intent().apply {
    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    data = Uri.parse("package:${context.packageName}")
}
context.startActivity(intent)
```

### Service Persistence Strategies

The service uses multiple strategies to stay alive:

1. **Foreground Service** - Highest priority, shows persistent notification
2. **START_STICKY** - System restarts service if killed
3. **onTaskRemoved()** - Manually restarts service when task is removed
4. **BootReceiver** - Restarts on device boot
5. **WakeLock** - Prevents system from sleeping during monitoring

## Troubleshooting

### Service Not Starting

**Check logs:**
```bash
adb logcat | grep FallDetectionService
```

**Verify user is logged in:**
- SessionManager must have valid userId
- Check DataStore preferences

**Check permissions:**
- CALL_PHONE must be granted
- Verify in Settings > Apps > Epsilon > Permissions

### Calls Not Placing

**Permission Issue:**
- Ensure CALL_PHONE is granted
- Check: `emergencyCallManager.hasCallPermission()`

**No Emergency Contact:**
- User must set emergency contact first
- Check: `emergencyContactManager.hasEmergencyContact()`

**Device Has No Phone:**
- Check: `emergencyCallManager.hasPhoneCapability()`
- Won't work on tablets without telephony

### Realtime Not Connecting

**Check Supabase Configuration:**
- Verify SUPABASE_URL is correct
- Verify SUPABASE_ANON_KEY is valid
- Check network connectivity

**Check RLS Policies:**
- User must have permission to read falls table
- Check policies in Supabase Dashboard

**Check Realtime is Enabled:**
```sql
alter publication supabase_realtime add table falls;
```

### Service Stops After Time

**Battery Optimization:**
- Disable battery optimization for the app
- Some manufacturers (Xiaomi, Huawei) aggressively kill background services
- Check manufacturer-specific battery settings

**Doze Mode:**
- Android may enter Doze mode and pause services
- Foreground services are exempt but may still be affected
- Consider using WorkManager for periodic checks as backup

## Security Considerations

### Emergency Contact Storage

- Stored in SharedPreferences (MODE_PRIVATE)
- Only accessible to the app
- Consider encrypting sensitive data in production

### Supabase Security

- Use Row Level Security (RLS) policies
- Limit access to user's own fall records
- Consider adding additional authentication checks

### Call Permissions

- CALL_PHONE is a dangerous permission
- User must explicitly grant
- Cannot be automatically granted
- Some users may deny for privacy reasons

## Production Recommendations

1. **Add Retry Logic** - Retry failed calls with exponential backoff
2. **Add Call Logging** - Log all emergency calls for audit trail
3. **Add SMS Fallback** - Send SMS if call fails
4. **Add Multiple Contacts** - Support multiple emergency contacts
5. **Add Confirmation Dialog** - Optional countdown before calling (configurable)
6. **Add Location Sharing** - Include location in emergency notification
7. **Add Medical Info** - Share medical information with emergency contact
8. **Add Two-Factor Verification** - Verify false positives before calling
9. **Add Analytics** - Track service health and call success rates
10. **Add Error Reporting** - Use Crashlytics or similar for production monitoring

## Dependencies Added

```gradle
implementation(platform("io.github.jan-tennert.supabase:bom:3.0.4"))
implementation("io.github.jan-tennert.supabase:postgrest-kt")
implementation("io.github.jan-tennert.supabase:realtime-kt")
implementation("io.github.jan-tennert.supabase:gotrue-kt")
```

## Files Created

```
app/src/main/java/com/epsilon/app/
├── data/
│   ├── emergency/
│   │   ├── EmergencyCallManager.kt
│   │   └── EmergencyContactManager.kt
│   ├── model/
│   │   └── Fall.kt
│   └── supabase/
│       └── SupabaseManager.kt
├── receiver/
│   └── BootReceiver.kt
├── service/
│   └── FallDetectionService.kt
└── ui/
    └── emergency/
        └── EmergencyContactScreen.kt
```

## Next Steps

1. Configure your Supabase credentials in `SupabaseManager.kt`
2. Build and run the app
3. Navigate to Emergency Contact screen
4. Grant permissions and set emergency contact
5. Test with a simulated fall event from wearable
6. Monitor logs to verify service operation
7. Test boot receiver by restarting device

## License

This implementation is part of the Epsilon Fall Detection System.
