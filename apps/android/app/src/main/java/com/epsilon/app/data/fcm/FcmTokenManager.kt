package com.epsilon.app.data.fcm

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Cloud Messaging tokens
 */
class FcmTokenManager(private val context: Context) {

    companion object {
        private const val TAG = "FcmTokenManager"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_TOKEN_SYNCED = "token_synced"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the current FCM token
     * If no token exists, it will be requested
     */
    suspend fun getToken(): String? {
        return try {
            // Try to get cached token first
            val cachedToken = getCachedToken()
            if (cachedToken != null) {
                return cachedToken
            }

            // Request new token
            val token = FirebaseMessaging.getInstance().token.await()
            saveToken(token)
            Log.d(TAG, "FCM token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }

    /**
     * Get the cached FCM token
     */
    fun getCachedToken(): String? {
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Save FCM token locally
     */
    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_FCM_TOKEN, token)
            .putBoolean(KEY_TOKEN_SYNCED, false)
            .apply()
        Log.d(TAG, "FCM token saved locally")
    }

    /**
     * Mark token as synced with server
     */
    fun markTokenAsSynced() {
        prefs.edit()
            .putBoolean(KEY_TOKEN_SYNCED, true)
            .apply()
    }

    /**
     * Check if token needs to be synced with server
     */
    fun needsSync(): Boolean {
        return !prefs.getBoolean(KEY_TOKEN_SYNCED, false) && getCachedToken() != null
    }

    /**
     * Clear stored token
     */
    fun clearToken() {
        prefs.edit()
            .remove(KEY_FCM_TOKEN)
            .remove(KEY_TOKEN_SYNCED)
            .apply()
        Log.d(TAG, "FCM token cleared")
    }

    /**
     * Subscribe to a topic
     */
    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic: $topic", e)
            false
        }
    }

    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from topic: $topic", e)
            false
        }
    }
}
