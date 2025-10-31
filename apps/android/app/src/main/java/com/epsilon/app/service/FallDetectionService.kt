package com.epsilon.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.epsilon.app.MainActivity
import com.epsilon.app.R
import com.epsilon.app.data.emergency.EmergencyCallManager
import com.epsilon.app.data.emergency.EmergencyContactManager
import com.epsilon.app.data.model.Fall
import com.epsilon.app.data.supabase.SupabaseManager
import kotlinx.coroutines.*

class FallDetectionService : Service() {

    companion object {
        private const val TAG = "FallDetectionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "fall_detection_channel"
        private const val CHANNEL_NAME = "Fall Detection Service"
        private const val WAKE_LOCK_TAG = "FallDetection:WakeLock"
        
        const val ACTION_START_SERVICE = "com.epsilon.app.START_FALL_DETECTION"
        const val ACTION_STOP_SERVICE = "com.epsilon.app.STOP_FALL_DETECTION"
        
        fun startService(context: Context) {
            val intent = Intent(context, FallDetectionService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, FallDetectionService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }

    private lateinit var supabaseManager: SupabaseManager
    private lateinit var emergencyContactManager: EmergencyContactManager
    private lateinit var emergencyCallManager: EmergencyCallManager
    private lateinit var notificationManager: NotificationManager
    
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var isSubscribed = false
    private var serviceInitialized = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        try {
            supabaseManager = SupabaseManager(applicationContext)
            emergencyContactManager = EmergencyContactManager(applicationContext)
            emergencyCallManager = EmergencyCallManager(applicationContext)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            createNotificationChannel()
            acquireWakeLock()
            serviceInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing service", e)
            serviceInitialized = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        
        if (!serviceInitialized) {
            Log.e(TAG, "Service not properly initialized, stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundService()
                startFallDetection()
            }
            ACTION_STOP_SERVICE -> {
                stopFallDetection()
                stopSelf()
            }
        }
        
        // Service will restart if killed by the system
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification("Monitoring for fall detection")
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Service started in foreground")
    }

    private fun startFallDetection() {
        if (isSubscribed) {
            Log.d(TAG, "Already subscribed to fall detection")
            return
        }

        serviceScope.launch {
            try {
                // Check if emergency contact is configured
                if (!emergencyContactManager.hasEmergencyContact()) {
                    Log.w(TAG, "No emergency contact configured")
                    updateNotification("No emergency contact configured")
                    return@launch
                }

                // Get user ID
                val userId = supabaseManager.getUserId()
                if (userId.isNullOrBlank()) {
                    Log.e(TAG, "User ID not found")
                    updateNotification("User not logged in")
                    return@launch
                }

                Log.d(TAG, "Starting fall detection for user: $userId")
                updateNotification("Monitoring for falls")

                // Subscribe to Supabase Realtime
                supabaseManager.subscribeToFalls(userId) { fall ->
                    handleFallDetected(fall)
                }
                
                isSubscribed = true
                Log.d(TAG, "Successfully subscribed to fall detection")

            } catch (e: Exception) {
                Log.e(TAG, "Error starting fall detection", e)
                updateNotification("Error: ${e.message}")
            }
        }
    }

    private fun stopFallDetection() {
        serviceScope.launch {
            try {
                supabaseManager.unsubscribe()
                isSubscribed = false
                Log.d(TAG, "Stopped fall detection")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping fall detection", e)
            }
        }
    }

    private fun handleFallDetected(fall: Fall) {
        Log.w(TAG, "FALL DETECTED! Fall ID: ${fall.id}, Time: ${fall.detectedAt}")
        
        // Update notification
        updateNotification("⚠️ Fall detected! Calling emergency contact...")
        
        // Get emergency contact
        val emergencyNumber = emergencyContactManager.getEmergencyContact()
        if (emergencyNumber.isNullOrBlank()) {
            Log.e(TAG, "Emergency contact not configured!")
            updateNotification("❌ No emergency contact configured")
            return
        }
        
        // Check permissions
        if (!emergencyCallManager.hasCallPermission()) {
            Log.e(TAG, "Call permission not granted!")
            updateNotification("❌ Call permission not granted")
            return
        }
        
        // Place emergency call
        val callPlaced = emergencyCallManager.placeEmergencyCall(emergencyNumber)
        
        if (callPlaced) {
            Log.d(TAG, "Emergency call placed successfully to: $emergencyNumber")
            updateNotification("📞 Calling emergency contact: $emergencyNumber")
        } else {
            Log.e(TAG, "Failed to place emergency call")
            updateNotification("❌ Failed to place emergency call")
            
            // Fallback: Show dialer
            emergencyCallManager.showDialerWithNumber(emergencyNumber)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors for fall detection events"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detection Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Replace with your icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes
                Log.d(TAG, "Wake lock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        stopFallDetection()
        releaseWakeLock()
        serviceScope.cancel()
        serviceScope.launch {
            supabaseManager.close()
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Task removed, restarting service")
        // Restart service when task is removed
        val restartServiceIntent = Intent(applicationContext, FallDetectionService::class.java).apply {
            action = ACTION_START_SERVICE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(restartServiceIntent)
        } else {
            applicationContext.startService(restartServiceIntent)
        }
        super.onTaskRemoved(rootIntent)
    }
}
