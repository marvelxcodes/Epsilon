package com.epsilon.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.epsilon.app.data.model.Medication
import com.epsilon.app.receiver.MedicationAlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "AlarmScheduler"
        private const val REQUEST_CODE_BASE = 10000
    }
    
    /**
     * Schedule alarms for all times specified in the medication
     */
    fun scheduleMedicationAlarms(medication: Medication) {
        if (medication.isActive != "true" || medication.reminderEnabled != "true") {
            Log.d(TAG, "Skipping alarm schedule for inactive or disabled medication: ${medication.name}")
            return
        }
        
        val times = medication.time.split(",").map { it.trim() }
        
        times.forEachIndexed { index, time ->
            scheduleAlarm(medication, time, index)
        }
        
        Log.d(TAG, "Scheduled ${times.size} alarms for medication: ${medication.name}")
    }
    
    /**
     * Schedule a single alarm for a specific time
     */
    private fun scheduleAlarm(medication: Medication, time: String, timeIndex: Int) {
        try {
            val calendar = Calendar.getInstance().apply {
                val timeParts = time.split(":")
                if (timeParts.size != 2) {
                    Log.e(TAG, "Invalid time format: $time")
                    return
                }
                
                val hour = timeParts[0].toIntOrNull()
                val minute = timeParts[1].toIntOrNull()
                
                if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                    Log.e(TAG, "Invalid time values: $time")
                    return
                }
                
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If the time has already passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                putExtra("medication_id", medication.id)
                putExtra("medication_name", medication.name)
                putExtra("medication_dosage", medication.dosage)
                putExtra("time", time)
            }
            
            val requestCode = getRequestCode(medication.id, timeIndex)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Use setAlarmClock for maximum reliability even when device is locked
            // This shows an alarm icon in status bar and is not affected by Doze mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent(context, context::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(
                    calendar.timeInMillis,
                    showIntent
                )
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Scheduled alarm for ${medication.name} at $time (Request code: $requestCode, Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)})")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm for ${medication.name} at $time", e)
        }
    }
    
    /**
     * Cancel all alarms for a specific medication
     */
    fun cancelAlarmsForMedication(medicationId: String) {
        try {
            // Cancel up to 10 possible time slots (should be enough for most cases)
            for (i in 0 until 10) {
                val requestCode = getRequestCode(medicationId, i)
                val intent = Intent(context, MedicationAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            
            Log.d(TAG, "Cancelled alarms for medication: $medicationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarms for medication: $medicationId", e)
        }
    }
    
    /**
     * Reschedule alarm for next occurrence (called after alarm fires)
     */
    fun rescheduleAlarm(medicationId: String, time: String, timeIndex: Int) {
        try {
            val calendar = Calendar.getInstance().apply {
                val timeParts = time.split(":")
                if (timeParts.size != 2) return
                
                val hour = timeParts[0].toIntOrNull() ?: return
                val minute = timeParts[1].toIntOrNull() ?: return
                
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // Schedule for tomorrow
                add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                putExtra("medication_id", medicationId)
                putExtra("time", time)
            }
            
            val requestCode = getRequestCode(medicationId, timeIndex)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Use setAlarmClock for maximum reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent(context, context::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(
                    calendar.timeInMillis,
                    showIntent
                )
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Rescheduled alarm for $medicationId at $time for tomorrow (${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)})")
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling alarm", e)
        }
    }
    
    /**
     * Generate a unique request code for each medication and time combination
     */
    private fun getRequestCode(medicationId: String, timeIndex: Int): Int {
        // Use medication ID hash combined with time index to create unique request code
        val idHash = medicationId.hashCode() and 0xFFFFFF // Limit to 24 bits
        return REQUEST_CODE_BASE + (idHash * 10) + timeIndex
    }
}
