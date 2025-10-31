package com.epsilon.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.epsilon.app.data.emergency.EmergencyCallManager
import com.epsilon.app.data.emergency.EmergencyContactManager

/**
 * Broadcast receiver for handling emergency call triggers
 * Can be triggered from FCM notifications or other sources
 */
class EmergencyCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "EmergencyCallReceiver"
        const val ACTION_TRIGGER_EMERGENCY_CALL = "com.epsilon.app.TRIGGER_EMERGENCY_CALL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_EMERGENCY_CALL) {
            return
        }

        Log.d(TAG, "Emergency call trigger received")

        val emergencyType = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_EMERGENCY_TYPE)
        val userId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_USER_ID)
        val reportId = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_REPORT_ID)
        val timestamp = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_TIMESTAMP)

        Log.d(TAG, "Emergency details - Type: $emergencyType, User: $userId, Report: $reportId")

        // Get emergency contact and place call
        val contactManager = EmergencyContactManager(context)
        val callManager = EmergencyCallManager(context)

        val emergencyContact = contactManager.getEmergencyContact()
        val contactName = contactManager.getEmergencyContactName()

        if (emergencyContact != null) {
            Log.d(TAG, "Placing emergency call to: ${contactName ?: "Unknown"} ($emergencyContact)")
            
            if (callManager.hasCallPermission()) {
                val success = callManager.placeEmergencyCall(emergencyContact)
                if (success) {
                    Log.d(TAG, "Emergency call placed successfully")
                } else {
                    Log.e(TAG, "Failed to place emergency call")
                    // Fallback to dialer
                    callManager.showDialerWithNumber(emergencyContact)
                }
            } else {
                Log.e(TAG, "Call permission not granted, showing dialer")
                callManager.showDialerWithNumber(emergencyContact)
            }
        } else {
            Log.e(TAG, "No emergency contact configured")
        }
    }
}
