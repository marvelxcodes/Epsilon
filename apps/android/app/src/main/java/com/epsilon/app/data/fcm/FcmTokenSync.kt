package com.epsilon.app.data.fcm

import android.content.Context
import android.util.Log
import com.epsilon.app.BuildConfig
import com.epsilon.app.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Sync FCM token with backend server
 */
class FcmTokenSync(
    private val context: Context,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "FcmTokenSync"
    }

    private val tokenManager = FcmTokenManager(context)
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    @Serializable
    data class TokenUpdateRequest(
        val fcmToken: String,
        val deviceType: String = "android"
    )

    /**
     * Sync FCM token with backend
     */
    suspend fun syncToken(): Boolean {
        return try {
            Log.d(TAG, "Starting FCM token sync...")
            
            val token = tokenManager.getToken()
            if (token == null) {
                Log.e(TAG, "No FCM token available - Firebase may not be initialized")
                return false
            }
            
            Log.d(TAG, "FCM token retrieved: ${token.take(20)}...")

            val authToken = sessionManager.token.first()
            if (authToken == null) {
                Log.e(TAG, "No auth token available - user not logged in")
                return false
            }
            
            Log.d(TAG, "Auth token retrieved, sending to backend: ${BuildConfig.BACKEND_URL}")

            val response: HttpResponse = client.post("${BuildConfig.BACKEND_URL}/api/user/fcm-token") {
                headers {
                    // Better Auth uses session cookies, not Bearer tokens
                    append("Cookie", "better-auth.session_token=$authToken")
                }
                contentType(ContentType.Application.Json)
                setBody(TokenUpdateRequest(fcmToken = token))
            }

            if (response.status.value in 200..299) {
                tokenManager.markTokenAsSynced()
                Log.d(TAG, "✅ FCM token synced successfully!")
                true
            } else {
                Log.e(TAG, "❌ Failed to sync FCM token: ${response.status}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing FCM token: ${e.message}", e)
            false
        }
    }

    /**
     * Check and sync token if needed
     */
    suspend fun syncIfNeeded() {
        if (tokenManager.needsSync()) {
            syncToken()
        }
    }

    fun close() {
        client.close()
    }
}
