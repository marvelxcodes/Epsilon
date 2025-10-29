# Medication Reminder App - API Documentation

Base URL: `http://localhost:3001/api`

All endpoints require authentication. Include the session cookie in requests after signing in.

---

## Authentication

### Sign Up
**POST** `/auth/sign-up/email`

Creates a new user account.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "user": {
    "id": "user_id",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "session": {
    "token": "session_token",
    "expiresAt": "2025-11-28T23:00:00.000Z"
  }
}
```

---

### Sign In
**POST** `/auth/sign-in/email`

Authenticates an existing user.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "user": {
    "id": "user_id",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "session": {
    "token": "session_token",
    "expiresAt": "2025-11-28T23:00:00.000Z"
  }
}
```

---

### Sign Out
**POST** `/auth/sign-out`

Signs out the current user.

**Response (200):**
```json
{
  "success": true
}
```

---

## Device Management

### Register or Update Device
**POST** `/device`

Registers a new device or updates an existing one. If a device with the same `deviceToken` exists for the user, it will be updated instead of creating a new one.

**Request Body:**
```json
{
  "deviceName": "Samsung Galaxy S23",
  "deviceToken": "fcm_token_here",
  "deviceModel": "SM-S911B",
  "osVersion": "Android 14",
  "appVersion": "1.0.0"
}
```

**Response (200):**
```json
{
  "device": {
    "id": "device_id",
    "userId": "user_id",
    "deviceName": "Samsung Galaxy S23",
    "deviceToken": "fcm_token_here",
    "deviceModel": "SM-S911B",
    "osVersion": "Android 14",
    "appVersion": "1.0.0",
    "lastActiveAt": "2025-10-29T00:00:00.000Z",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  },
  "message": "Device registered successfully"
}
```

---

### Check if Device Exists
**GET** `/device?deviceToken=fcm_token_here`

Checks if a specific device is registered for the current user.

**Query Parameters:**
- `deviceToken` (required): The device token to check

**Response (200):**
```json
{
  "exists": true,
  "device": {
    "id": "device_id",
    "userId": "user_id",
    "deviceName": "Samsung Galaxy S23",
    "deviceToken": "fcm_token_here",
    "deviceModel": "SM-S911B",
    "osVersion": "Android 14",
    "appVersion": "1.0.0",
    "lastActiveAt": "2025-10-29T00:00:00.000Z",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  }
}
```

---

### Get All Devices
**GET** `/device`

Gets all devices registered for the current user.

**Response (200):**
```json
{
  "devices": [
    {
      "id": "device_id",
      "userId": "user_id",
      "deviceName": "Samsung Galaxy S23",
      "deviceToken": "fcm_token_here",
      "deviceModel": "SM-S911B",
      "osVersion": "Android 14",
      "appVersion": "1.0.0",
      "lastActiveAt": "2025-10-29T00:00:00.000Z",
      "createdAt": "2025-10-29T00:00:00.000Z",
      "updatedAt": "2025-10-29T00:00:00.000Z"
    }
  ]
}
```

---

### Update Device
**PATCH** `/device`

Updates device information.

**Request Body:**
```json
{
  "deviceId": "device_id",
  "deviceName": "Samsung Galaxy S23 Ultra",
  "appVersion": "1.0.1"
}
```

**Response (200):**
```json
{
  "device": {
    "id": "device_id",
    "userId": "user_id",
    "deviceName": "Samsung Galaxy S23 Ultra",
    "deviceToken": "fcm_token_here",
    "deviceModel": "SM-S911B",
    "osVersion": "Android 14",
    "appVersion": "1.0.1",
    "lastActiveAt": "2025-10-29T00:00:00.000Z",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  },
  "message": "Device updated successfully"
}
```

---

## Medicine Management

### Get All Medicines
**GET** `/medicine`

Gets all medicines for the current user.

**Query Parameters:**
- `activeOnly` (optional): Set to `true` to only get active medicines

**Response (200):**
```json
{
  "medicines": [
    {
      "id": "medicine_id",
      "userId": "user_id",
      "name": "Aspirin",
      "dosage": "100mg",
      "frequency": "daily",
      "time": "08:00,20:00",
      "startDate": "2025-10-29T00:00:00.000Z",
      "endDate": "2025-11-29T00:00:00.000Z",
      "notes": "Take with food",
      "isActive": "true",
      "reminderEnabled": "true",
      "createdAt": "2025-10-29T00:00:00.000Z",
      "updatedAt": "2025-10-29T00:00:00.000Z"
    }
  ]
}
```

---

### Create Medicine
**POST** `/medicine`

Creates a new medicine reminder.

**Request Body:**
```json
{
  "name": "Aspirin",
  "dosage": "100mg",
  "frequency": "daily",
  "time": "08:00,20:00",
  "startDate": "2025-10-29",
  "endDate": "2025-11-29",
  "notes": "Take with food",
  "reminderEnabled": true
}
```

**Field Descriptions:**
- `name` (required): Medicine name
- `dosage` (required): Dosage amount (e.g., "100mg", "2 tablets")
- `frequency` (required): How often to take (e.g., "daily", "twice daily", "weekly")
- `time` (required): Time(s) to take medicine. Use comma-separated for multiple times (e.g., "08:00" or "08:00,14:00,20:00")
- `startDate` (required): When to start taking the medicine (ISO date string)
- `endDate` (optional): When to stop taking the medicine (ISO date string)
- `notes` (optional): Additional notes
- `reminderEnabled` (optional): Enable/disable reminders (default: true)

**Response (200):**
```json
{
  "medicine": {
    "id": "medicine_id",
    "userId": "user_id",
    "name": "Aspirin",
    "dosage": "100mg",
    "frequency": "daily",
    "time": "08:00,20:00",
    "startDate": "2025-10-29T00:00:00.000Z",
    "endDate": "2025-11-29T00:00:00.000Z",
    "notes": "Take with food",
    "isActive": "true",
    "reminderEnabled": "true",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  },
  "message": "Medicine created successfully"
}
```

---

### Get Single Medicine
**GET** `/medicine/:id`

Gets a specific medicine by ID.

**Response (200):**
```json
{
  "medicine": {
    "id": "medicine_id",
    "userId": "user_id",
    "name": "Aspirin",
    "dosage": "100mg",
    "frequency": "daily",
    "time": "08:00,20:00",
    "startDate": "2025-10-29T00:00:00.000Z",
    "endDate": "2025-11-29T00:00:00.000Z",
    "notes": "Take with food",
    "isActive": "true",
    "reminderEnabled": "true",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  }
}
```

---

### Update Medicine
**PATCH** `/medicine/:id`

Updates a medicine. All fields are optional.

**Request Body:**
```json
{
  "name": "Aspirin 100mg",
  "dosage": "150mg",
  "frequency": "twice daily",
  "time": "08:00,20:00",
  "notes": "Take with water",
  "isActive": true,
  "reminderEnabled": false
}
```

**Response (200):**
```json
{
  "medicine": {
    "id": "medicine_id",
    "userId": "user_id",
    "name": "Aspirin 100mg",
    "dosage": "150mg",
    "frequency": "twice daily",
    "time": "08:00,20:00",
    "startDate": "2025-10-29T00:00:00.000Z",
    "endDate": "2025-11-29T00:00:00.000Z",
    "notes": "Take with water",
    "isActive": "true",
    "reminderEnabled": "false",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  },
  "message": "Medicine updated successfully"
}
```

---

### Delete Medicine
**DELETE** `/medicine/:id`

Deletes a medicine.

**Response (200):**
```json
{
  "message": "Medicine deleted successfully"
}
```

---

## Emergency Actions

Emergency actions are triggered when a medicine reminder is missed or requires immediate attention.

### Get All Emergency Actions
**GET** `/emergency-action`

Gets all emergency actions for the current user, ordered by priority (1 is highest).

**Response (200):**
```json
{
  "actions": [
    {
      "id": "action_id",
      "userId": "user_id",
      "actionType": "call",
      "actionData": "+1234567890",
      "priority": 1,
      "isEnabled": "true",
      "createdAt": "2025-10-29T00:00:00.000Z",
      "updatedAt": "2025-10-29T00:00:00.000Z"
    },
    {
      "id": "action_id_2",
      "userId": "user_id",
      "actionType": "alarm",
      "actionData": "emergency_alarm.mp3",
      "priority": 2,
      "isEnabled": "true",
      "createdAt": "2025-10-29T00:00:00.000Z",
      "updatedAt": "2025-10-29T00:00:00.000Z"
    }
  ]
}
```

---

### Create Emergency Action
**POST** `/emergency-action`

Creates a new emergency action.

**Request Body:**
```json
{
  "actionType": "call",
  "actionData": "+1234567890",
  "priority": 1,
  "isEnabled": true
}
```

**Action Types and Data:**
- `call`: `actionData` should be a phone number (e.g., "+1234567890")
- `message`: `actionData` should be a phone number to send SMS to (e.g., "+1234567890")
- `alarm`: `actionData` should be the alarm sound identifier or file path (e.g., "emergency_alarm.mp3")

**Field Descriptions:**
- `actionType` (required): Type of action - "call", "message", or "alarm"
- `actionData` (required): Data for the action (phone number or alarm sound)
- `priority` (optional): Priority level (1 is highest, default: 1)
- `isEnabled` (optional): Enable/disable this action (default: true)

**Response (200):**
```json
{
  "action": {
    "id": "action_id",
    "userId": "user_id",
    "actionType": "call",
    "actionData": "+1234567890",
    "priority": 1,
    "isEnabled": "true",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  },
  "message": "Emergency action created successfully"
}
```

---

### Get Single Emergency Action
**GET** `/emergency-action/:id`

Gets a specific emergency action by ID.

**Response (200):**
```json
{
  "action": {
    "id": "action_id",
    "userId": "user_id",
    "actionType": "call",
    "actionData": "+1234567890",
    "priority": 1,
    "isEnabled": "true",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  }
}
```

---

### Update Emergency Action
**PATCH** `/emergency-action/:id`

Updates an emergency action. All fields are optional.

**Request Body:**
```json
{
  "actionType": "message",
  "actionData": "+0987654321",
  "priority": 2,
  "isEnabled": false
}
```

**Response (200):**
```json
{
  "action": {
    "id": "action_id",
    "userId": "user_id",
    "actionType": "message",
    "actionData": "+0987654321",
    "priority": 2,
    "isEnabled": "false",
    "createdAt": "2025-10-29T00:00:00.000Z",
    "updatedAt": "2025-10-29T00:00:00.000Z"
  },
  "message": "Emergency action updated successfully"
}
```

---

### Delete Emergency Action
**DELETE** `/emergency-action/:id`

Deletes an emergency action.

**Response (200):**
```json
{
  "message": "Emergency action deleted successfully"
}
```

---

## Error Responses

All endpoints may return the following error responses:

### 401 Unauthorized
```json
{
  "error": "Unauthorized"
}
```

### 400 Bad Request
```json
{
  "error": "Error message describing what's wrong"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Error message"
}
```

---

## Usage Flow for Android App

### 1. Initial Setup
1. User signs up or signs in
2. Register the device with FCM token
3. Set up emergency actions (at least one recommended)

### 2. Medicine Management
1. User creates medicine reminders with dosage, frequency, and times
2. App schedules local notifications based on medicine times
3. When medicine is taken, log it (you can implement a medicine log endpoint if needed)

### 3. Emergency Actions
1. Set up emergency actions in priority order
2. When a medicine reminder is missed or critical, trigger emergency actions
3. Execute actions based on priority (call, message, or alarm)

### 4. Sync
1. Periodically sync medicine list and emergency actions
2. Update device `lastActiveAt` timestamp
3. Handle offline scenarios with local caching

---

## Data Models Summary

### Device
- `id`: Unique identifier
- `userId`: Owner user ID
- `deviceName`: Device name (e.g., "Samsung Galaxy S23")
- `deviceToken`: FCM/push notification token
- `deviceModel`: Device model
- `osVersion`: OS version
- `appVersion`: App version
- `lastActiveAt`: Last time device was active
- `createdAt`, `updatedAt`: Timestamps

### Medicine
- `id`: Unique identifier
- `userId`: Owner user ID
- `name`: Medicine name
- `dosage`: Dosage amount
- `frequency`: How often to take
- `time`: Time(s) to take (comma-separated for multiple)
- `startDate`: Start date
- `endDate`: End date (optional)
- `notes`: Additional notes
- `isActive`: Whether medicine is currently active
- `reminderEnabled`: Whether reminders are enabled
- `createdAt`, `updatedAt`: Timestamps

### Emergency Action
- `id`: Unique identifier
- `userId`: Owner user ID
- `actionType`: "call", "message", or "alarm"
- `actionData`: Phone number or alarm sound
- `priority`: Priority level (1 is highest)
- `isEnabled`: Whether action is enabled
- `createdAt`, `updatedAt`: Timestamps
