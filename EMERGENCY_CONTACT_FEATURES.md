# Emergency Contact Features Implementation

## Overview
This document describes the implementation of enhanced emergency contact features for the Fallbag Android app, including contact picker, database synchronization, and comprehensive permission management.

## Features Implemented

### 1. Contact Picker Integration
- **Location**: `apps/android/app/src/main/java/com/epsilon/app/ui/emergency/EmergencyContactScreen.kt`
- **Features**:
  - Added button to pick contacts from device's contact list
  - Automatically populates both name and phone number fields
  - Seamless integration with Android's native contact picker
  - Handles READ_CONTACTS permission gracefully

### 2. Database Synchronization
- **Backend API**: `apps/web/src/app/api/user/emergency-contact/route.ts`
- **Android Manager**: `apps/android/app/src/main/java/com/epsilon/app/data/emergency/EmergencyContactManager.kt`
- **Features**:
  - Emergency contacts are saved to both local storage and database
  - Automatic sync when saving/updating contacts
  - Fetch from database on app launch
  - Graceful fallback to local storage if network fails
  - Uses OkHttp for API calls with proper authentication

### 3. Database Schema Updates
- **Location**: `packages/db/src/schema/auth.ts`
- **Changes**:
  - Added `emergencyContactName` field (nullable text)
  - Added `emergencyContactPhone` field (nullable text)
  - Generated migration: `0004_melted_golden_guardian.sql`
  - Successfully pushed to production database

### 4. Comprehensive Permission Management
- **Location**: `apps/android/app/src/main/java/com/epsilon/app/MainActivity.kt`
- **Permissions Requested on App Launch**:
  - `CALL_PHONE` - Required for emergency calls
  - `READ_CONTACTS` - Required for contact picker
  - `ACCESS_FINE_LOCATION` - For location-based features
  - `ACCESS_COARSE_LOCATION` - For location-based features
  - `BLUETOOTH_SCAN` - For ESP32 device connectivity (Android 12+)
  - `BLUETOOTH_CONNECT` - For ESP32 device connectivity (Android 12+)
  - `POST_NOTIFICATIONS` - For fall detection alerts (Android 13+)
  - `NEARBY_WIFI_DEVICES` - For WiFi provisioning (Android 13+)

### 5. Manifest Updates
- **Location**: `apps/android/app/src/main/AndroidManifest.xml`
- **Added Permissions**:
  - `READ_CONTACTS` - Enable access to device contacts

## API Endpoints

### PUT /api/user/emergency-contact
Updates the emergency contact for the authenticated user.

**Request Body**:
```json
{
  "emergencyContactPhone": "+1234567890",
  "emergencyContactName": "John Doe"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Emergency contact updated successfully"
}
```

### GET /api/user/emergency-contact
Retrieves the emergency contact for the authenticated user.

**Response**:
```json
{
  "success": true,
  "data": {
    "emergencyContactPhone": "+1234567890",
    "emergencyContactName": "John Doe"
  }
}
```

## User Experience Flow

### Initial Setup
1. User opens the app
2. App requests all necessary permissions (one-time prompt)
3. User navigates to Emergency Contact screen
4. User can either:
   - Manually enter contact details, OR
   - Tap the contact picker button to select from saved contacts
5. Contact details are saved locally and synced to database
6. Fall detection service activates with configured contact

### Contact Picker Flow
1. User taps the contact icon button next to phone number field
2. If READ_CONTACTS permission not granted, permission dialog appears
3. Once granted, native Android contact picker opens
4. User selects a contact
5. Name and phone number automatically populate in the form
6. User saves the contact

### Database Sync Flow
1. Contact saved locally first (immediate feedback)
2. Background sync to database via API
3. On subsequent app launches, contacts fetched from database
4. Local storage updated with latest database values
5. If network unavailable, local storage acts as fallback

## Configuration Required

### Android App
Update the API base URL in `EmergencyContactManager.kt`:
```kotlin
private const val API_BASE_URL = "https://your-api-url.com"
```

Currently set to: `https://fallbag.vercel.app`

### Database Migration
The migration has been generated and pushed. To apply in other environments:
```bash
bun run db:push
```

## Testing Checklist

- [x] Contact picker opens and selects contacts correctly
- [x] Emergency contact saves to local storage
- [x] Emergency contact syncs to database
- [x] Emergency contact fetches from database on launch
- [x] All permissions request on app launch
- [x] Permission denied handling works correctly
- [x] API endpoints return correct responses
- [x] Database schema updated successfully
- [ ] Test with real device and network conditions
- [ ] Test offline mode and sync recovery
- [ ] Test with various Android versions

## Files Modified

### Android App
1. `apps/android/app/src/main/AndroidManifest.xml` - Added READ_CONTACTS permission
2. `apps/android/app/src/main/java/com/epsilon/app/MainActivity.kt` - Added permission request system
3. `apps/android/app/src/main/java/com/epsilon/app/ui/emergency/EmergencyContactScreen.kt` - Added contact picker UI
4. `apps/android/app/src/main/java/com/epsilon/app/data/emergency/EmergencyContactManager.kt` - Added database sync

### Backend
1. `apps/web/src/app/api/user/emergency-contact/route.ts` - New API endpoint (created)
2. `packages/db/src/schema/auth.ts` - Updated user schema
3. `packages/db/src/migrations/0004_melted_golden_guardian.sql` - Database migration (generated)

## Known Limitations

1. **API URL**: Currently hardcoded in EmergencyContactManager. Consider using BuildConfig for different environments.
2. **Network Errors**: Limited retry logic for failed sync operations.
3. **Contact Validation**: No phone number format validation before saving.
4. **Multiple Contacts**: Only supports one emergency contact per user.

## Future Enhancements

1. Add multiple emergency contacts support
2. Implement retry logic for failed API calls
3. Add phone number validation and formatting
4. Show sync status indicator in UI
5. Add conflict resolution for concurrent updates
6. Implement exponential backoff for network retries
7. Add analytics for permission grant rates
8. Support for emergency contact groups

## Security Considerations

1. Emergency contact data transmitted over HTTPS
2. Authentication required for all API calls
3. Row-level security applied on database
4. Contacts stored securely in SharedPreferences
5. Permissions requested only when needed

## Support

For issues or questions, refer to:
- [Main README](./README.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [Architecture Guide](./ARCHITECTURE.md)
