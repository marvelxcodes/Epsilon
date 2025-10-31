package com.epsilon.app.data.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class SessionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SessionManager"
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val EMAIL_VERIFIED_KEY = booleanPreferencesKey("email_verified")
        private val CREATED_AT_KEY = stringPreferencesKey("created_at")
        private val UPDATED_AT_KEY = stringPreferencesKey("updated_at")
    }
    
    val authToken: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading auth token", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    
    val token: Flow<String?> = authToken
    
    val userId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user id", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_ID_KEY]
        }
    
    val userName: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user name", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_NAME_KEY]
        }
    
    val userEmail: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user email", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    
    val emailVerified: Flow<Boolean?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading email verified", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[EMAIL_VERIFIED_KEY]
        }
    
    val createdAt: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading created at", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CREATED_AT_KEY]
        }
    
    val updatedAt: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading updated at", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[UPDATED_AT_KEY]
        }
    
    // Flow that emits login status with error handling
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading login status", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val token = preferences[AUTH_TOKEN_KEY]
            !token.isNullOrEmpty()
        }
    
    suspend fun saveAuthToken(token: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[AUTH_TOKEN_KEY] = token
            }
            Log.d(TAG, "Auth token saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving auth token", e)
            throw e
        }
    }
    
    suspend fun saveUserInfo(
        userId: String,
        name: String,
        email: String,
        emailVerified: Boolean = false,
        createdAt: String = "",
        updatedAt: String = ""
    ) {
        try {
            context.dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = userId
                preferences[USER_NAME_KEY] = name
                preferences[USER_EMAIL_KEY] = email
                preferences[EMAIL_VERIFIED_KEY] = emailVerified
                preferences[CREATED_AT_KEY] = createdAt
                preferences[UPDATED_AT_KEY] = updatedAt
            }
            Log.d(TAG, "User info saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user info", e)
            throw e
        }
    }
    
    suspend fun clearSession() {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            Log.d(TAG, "Session cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing session", e)
            throw e
        }
    }
    
    // Synchronous check for logged in status - safely gets first value
    suspend fun checkIsLoggedIn(): Boolean {
        return try {
            val token = authToken.first()
            val result = !token.isNullOrEmpty()
            Log.d(TAG, "Check is logged in: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }
}
