package com.epsilon.app.data.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class EmergencyCallManager(private val context: Context) {

    companion object {
        private const val TAG = "EmergencyCallManager"
    }

    /**
     * Check if CALL_PHONE permission is granted
     */
    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the app can make privileged calls
     */
    private fun canMakePrivilegedCalls(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                telecomManager?.let {
                    // Check if app has dialer role or can make privileged calls
                    context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELECOM) &&
                    hasCallPermission()
                } ?: false
            } else {
                hasCallPermission()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking privileged call capability", e)
            false
        }
    }

    /**
     * Place an emergency call to the given phone number
     * This will attempt to use the most direct method available
     */
    fun placeEmergencyCall(phoneNumber: String): Boolean {
        if (!hasCallPermission()) {
            Log.e(TAG, "CALL_PHONE permission not granted")
            return false
        }

        if (phoneNumber.isBlank()) {
            Log.e(TAG, "Phone number is blank")
            return false
        }

        return try {
            Log.d(TAG, "Attempting to place emergency call to: $phoneNumber")

            // Method 1: Try using TelecomManager for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                if (telecomManager != null && canMakePrivilegedCalls()) {
                    try {
                        val uri = Uri.parse("tel:$phoneNumber")
                        telecomManager.placeCall(uri, null)
                        Log.d(TAG, "Emergency call placed using TelecomManager")
                        return true
                    } catch (e: SecurityException) {
                        Log.w(TAG, "TelecomManager call failed, falling back to ACTION_CALL", e)
                    }
                }
            }

            // Method 2: Use ACTION_CALL (requires CALL_PHONE permission)
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                // Mark as emergency call to bypass certain restrictions
                putExtra("android.phone.extra.CALL_TYPE", "emergency")
            }
            context.startActivity(callIntent)
            Log.d(TAG, "Emergency call initiated using ACTION_CALL")
            true

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when placing call", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error placing emergency call", e)
            false
        }
    }

    /**
     * Show the dialer with the emergency number pre-filled
     * This is a fallback if direct calling is not available
     */
    fun showDialerWithNumber(phoneNumber: String): Boolean {
        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
            Log.d(TAG, "Dialer opened with emergency number")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialer", e)
            false
        }
    }

    /**
     * Check if device has phone capability
     */
    fun hasPhoneCapability(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
}
