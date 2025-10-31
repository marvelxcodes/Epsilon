package com.epsilon.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.epsilon.app.MainActivity
import com.epsilon.app.R
import com.epsilon.app.utils.AlarmScheduler

class MedicationAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "MedicationAlarmReceiver"
        private const val CHANNEL_ID = "medication_reminders"
        private const val CHANNEL_NAME = "Medication Reminders"
        private const val NOTIFICATION_ID_BASE = 20000
        private const val WAKE_LOCK_TAG = "epsilon:medication_alarm"
        private const val WAKE_LOCK_TIMEOUT = 60000L // 1 minute
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Medication alarm received")
        
        // Acquire wake lock to ensure we can process even when device is sleeping
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK_TAG
        ).apply {
            acquire(WAKE_LOCK_TIMEOUT)
        }
        
        try {
            val medicationId = intent.getStringExtra("medication_id") ?: return
            val medicationName = intent.getStringExtra("medication_name") ?: "Medication"
            val medicationDosage = intent.getStringExtra("medication_dosage") ?: ""
            val time = intent.getStringExtra("time") ?: ""
            
            // Create notification channel
            createNotificationChannel(context)
            
            // Show notification
            showMedicationNotification(context, medicationId, medicationName, medicationDosage, time)
            
            // Reschedule for next day
            val timeIndex = getTimeIndex(medicationId, time)
            val alarmScheduler = AlarmScheduler(context)
            alarmScheduler.rescheduleAlarm(medicationId, time, timeIndex)
            
            Log.d(TAG, "Notification shown for $medicationName at $time")
        } finally {
            // Release wake lock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for medication schedules"
                enableVibration(true)
                enableLights(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true) // Bypass Do Not Disturb mode
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showMedicationNotification(
        context: Context,
        medicationId: String,
        medicationName: String,
        medicationDosage: String,
        time: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_reminders", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Time to take $medicationName")
            .setContentText("$medicationDosage at $time")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Don't forget to take your medication:\n\n💊 $medicationName\n📊 $medicationDosage\n⏰ Scheduled for: $time")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true) // Show notification even on lock screen
            .setOngoing(false)
            .setTimeoutAfter(300000) // Auto-dismiss after 5 minutes
            .build()
        
        // Generate unique notification ID based on medication ID
        val notificationId = NOTIFICATION_ID_BASE + medicationId.hashCode()
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "Notification shown with ID: $notificationId for $medicationName")
    }
    
    private fun getTimeIndex(medicationId: String, time: String): Int {
        // Simple hash to get consistent index for this medication-time combination
        return (medicationId + time).hashCode() and 0xF // Limit to 0-15
    }
}
