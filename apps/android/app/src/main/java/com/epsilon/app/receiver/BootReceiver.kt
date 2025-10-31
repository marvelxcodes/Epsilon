package com.epsilon.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.service.FallDetectionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Boot receiver to start fall detection service automatically when device boots
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        private const val TIMEOUT_MS = 5000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            Log.d(TAG, "Boot completed or app updated")
            
            // Temporarily disabled - Fall detection service requires Supabase configuration
            // Use SupervisorJob to prevent failure from cancelling other coroutines
            // val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            // val sessionManager = SessionManager(context.applicationContext)
            // 
            // scope.launch {
            //     try {
            //         // Add timeout to prevent hanging
            //         withTimeout(TIMEOUT_MS) {
            //             if (sessionManager.checkIsLoggedIn()) {
            //                 Log.d(TAG, "User logged in, starting fall detection service")
            //                 FallDetectionService.startService(context.applicationContext)
            //             } else {
            //                 Log.d(TAG, "User not logged in, service not started")
            //             }
            //         }
            //     } catch (e: Exception) {
            //         Log.e(TAG, "Error checking login status: ${e.message}", e)
            //     }
            // }
        }
    }
}
