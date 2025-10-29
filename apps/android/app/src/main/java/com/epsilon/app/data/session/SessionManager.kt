package com.epsilon.app.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class SessionManager(private val context: Context) {
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val EMAIL_VERIFIED_KEY = booleanPreferencesKey("email_verified")
        private val CREATED_AT_KEY = stringPreferencesKey("created_at")
        private val UPDATED_AT_KEY = stringPreferencesKey("updated_at")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    val token: Flow<String?> = authToken
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
    
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    val emailVerified: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_VERIFIED_KEY]
    }
    
    val createdAt: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CREATED_AT_KEY]
    }
    
    val updatedAt: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[UPDATED_AT_KEY]
    }
    
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
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
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
            preferences[EMAIL_VERIFIED_KEY] = emailVerified
            preferences[CREATED_AT_KEY] = createdAt
            preferences[UPDATED_AT_KEY] = updatedAt
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        var token: String? = null
        context.dataStore.data.map { preferences ->
            token = preferences[AUTH_TOKEN_KEY]
        }
        return !token.isNullOrEmpty()
    }
}
