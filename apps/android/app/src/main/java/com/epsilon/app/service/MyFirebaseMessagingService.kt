package com.epsilon.app.service

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications and triggers emergency calls even when app is closed
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val EXTRA_EMERGENCY_TYPE = "emergency_type"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_REPORT_ID = "report_id"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
    }

    /**
     * Called when a new FCM token is generated
     * This token should be sent to your server for sending push notifications
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")
        
        // TODO: Send token to your server
        // You can use a BroadcastReceiver or a local database to notify the app
        sendTokenToServer(token)
    }

    /**
     * Called when a message is received
     * This will be triggered even when the app is in background or closed
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Handle data payload
        message.data.let { data ->
            Log.d(TAG, "Message data payload: $data")
            
            when (data["type"]) {
                "EMERGENCY_CALL" -> handleEmergencyCall(data)
                else -> Log.w(TAG, "Unknown message type: ${data["type"]}")
            }
        }
    }

    /**
     * Handle emergency call trigger
     */
    private fun handleEmergencyCall(data: Map<String, String>) {
        Log.d(TAG, "Emergency call triggered via FCM")
        
        try {
            // Create intent to trigger emergency call
            val intent = Intent(this, EmergencyCallReceiver::class.java).apply {
                action = EmergencyCallReceiver.ACTION_TRIGGER_EMERGENCY_CALL
                putExtra(EXTRA_EMERGENCY_TYPE, "REMOTE_TRIGGER")
                putExtra(EXTRA_USER_ID, data["userId"] ?: "")
                putExtra(EXTRA_REPORT_ID, data["reportId"] ?: "")
                putExtra(EXTRA_TIMESTAMP, data["timestamp"] ?: "")
                
                // Optional location data
                data["latitude"]?.let { putExtra(EXTRA_LATITUDE, it) }
                data["longitude"]?.let { putExtra(EXTRA_LONGITUDE, it) }
            }
            
            // Send broadcast to trigger emergency call
            sendBroadcast(intent)
            
            Log.d(TAG, "Emergency call broadcast sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering emergency call", e)
        }
    }

    /**
     * Send FCM token to server
     * This should be implemented to update the user's FCM token in your backend
     */
    private fun sendTokenToServer(token: String) {
        // Store token locally for now
        val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        
        Log.d(TAG, "FCM token stored locally")
        
        // TODO: Send to backend API
        // This can be done using a WorkManager job or Ktor client
    }
}
