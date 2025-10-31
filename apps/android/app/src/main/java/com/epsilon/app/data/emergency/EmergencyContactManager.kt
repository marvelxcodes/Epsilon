package com.epsilon.app.data.emergency

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.epsilon.app.BuildConfig
import com.epsilon.app.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class EmergencyContactManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val sessionManager = SessionManager(context)
    private val client = OkHttpClient()

    companion object {
        private const val TAG = "EmergencyContactManager"
        private const val PREFS_NAME = "emergency_contacts"
        private const val KEY_EMERGENCY_CONTACT = "emergency_contact_number"
        private const val KEY_CONTACT_NAME = "emergency_contact_name"
    }

    /**
     * Save emergency contact number and sync with database
     */
    fun saveEmergencyContact(phoneNumber: String, contactName: String = "") {
        // Save locally first
        preferences.edit().apply {
            putString(KEY_EMERGENCY_CONTACT, phoneNumber)
            putString(KEY_CONTACT_NAME, contactName)
            apply()
        }
        
        // Sync with database
        syncToDatabase(phoneNumber, contactName)
    }

    /**
     * Sync emergency contact to database
     */
    private fun syncToDatabase(phoneNumber: String, contactName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.authToken.first()
                if (token.isNullOrBlank()) {
                    Log.w(TAG, "No access token available, skipping sync")
                    return@launch
                }

                val json = JSONObject().apply {
                    put("emergencyContactPhone", phoneNumber)
                    put("emergencyContactName", contactName)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_URL}/api/user/emergency-contact")
                    .put(body)
                    .addHeader("Cookie", "better-auth.session_token=$token")
                    .build()

                Log.d(TAG, "Syncing to: ${BuildConfig.BACKEND_URL}/api/user/emergency-contact")
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d(TAG, "Emergency contact synced to database successfully")
                } else {
                    Log.e(TAG, "Failed to sync emergency contact: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing emergency contact to database", e)
            }
        }
    }

    /**
     * Fetch emergency contact from database
     */
    suspend fun fetchFromDatabase(): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.authToken.first()
                if (token.isNullOrBlank()) {
                    Log.w(TAG, "No access token available")
                    return@withContext Pair(null, null)
                }

                Log.d(TAG, "Fetching from: ${BuildConfig.BACKEND_URL}/api/user/emergency-contact")
                val request = Request.Builder()
                    .url("${BuildConfig.BACKEND_URL}/api/user/emergency-contact")
                    .get()
                    .addHeader("Cookie", "better-auth.session_token=$token")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    val data = jsonResponse.optJSONObject("data")
                    val phone = data?.optString("emergencyContactPhone")
                    val name = data?.optString("emergencyContactName")
                    
                    // Save to local storage
                    if (!phone.isNullOrBlank() && phone != "null") {
                        preferences.edit().apply {
                            putString(KEY_EMERGENCY_CONTACT, phone)
                            putString(KEY_CONTACT_NAME, if (name == "null") "" else name)
                            apply()
                        }
                        Log.d(TAG, "Emergency contact fetched and saved: $phone")
                        return@withContext Pair(phone, if (name == "null") "" else name)
                    } else {
                        Log.d(TAG, "No emergency contact found in database")
                        return@withContext Pair(null, null)
                    }
                } else {
                    Log.e(TAG, "Failed to fetch emergency contact: ${response.code}")
                    return@withContext Pair(null, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching emergency contact from database", e)
                return@withContext Pair(null, null)
            }
        }
    }

    /**
     * Get emergency contact number
     */
    fun getEmergencyContact(): String? {
        return preferences.getString(KEY_EMERGENCY_CONTACT, null)
    }

    /**
     * Get emergency contact name
     */
    fun getEmergencyContactName(): String? {
        return preferences.getString(KEY_CONTACT_NAME, null)
    }

    /**
     * Check if emergency contact is configured
     */
    fun hasEmergencyContact(): Boolean {
        return !getEmergencyContact().isNullOrBlank()
    }

    /**
     * Clear emergency contact
     */
    fun clearEmergencyContact() {
        preferences.edit().apply {
            remove(KEY_EMERGENCY_CONTACT)
            remove(KEY_CONTACT_NAME)
            apply()
        }
        
        // Sync deletion to database
        syncToDatabase("", "")
    }
}
