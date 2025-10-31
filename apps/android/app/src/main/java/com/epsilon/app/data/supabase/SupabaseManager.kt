package com.epsilon.app.data.supabase

import android.content.Context
import android.util.Log
import com.epsilon.app.data.model.Fall
import com.epsilon.app.data.session.SessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SupabaseManager(private val context: Context) {

    companion object {
        private const val TAG = "SupabaseManager"
        // TODO: Replace with your Supabase credentials
        private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
        private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
        private const val FALLS_TABLE = "falls"
    }

    private val sessionManager = SessionManager(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val supabaseClient: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Realtime)
            install(Auth)
        }
    }

    /**
     * Get the Supabase client instance
     */
    fun getClient(): SupabaseClient = supabaseClient

    /**
     * Subscribe to fall detection events for the logged-in user
     * @param userId The user ID to filter fall events
     * @param onFallDetected Callback when a fall is detected
     */
    suspend fun subscribeToFalls(
        userId: String,
        onFallDetected: (Fall) -> Unit
    ) {
        try {
            Log.d(TAG, "Subscribing to falls for user: $userId")

            val channel = supabaseClient.channel("falls-channel")

            // Subscribe to INSERT events on the falls table
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = FALLS_TABLE
                filter = "user_id=eq.$userId"
            }

            // Listen to changes
            changeFlow
                .filter { change ->
                    // Only process INSERT events with is_fall = true
                    change is PostgresAction.Insert
                }
                .onEach { change ->
                    if (change is PostgresAction.Insert) {
                        val record = change.record
                        val isFall = record["is_fall"]?.jsonPrimitive?.content?.toBoolean() ?: false
                        if (isFall) {
                            Log.d(TAG, "Fall detected: $record")
                            // Parse the fall data
                            val fall = Fall(
                                id = record["id"]?.jsonPrimitive?.content ?: "",
                                userId = record["user_id"]?.jsonPrimitive?.content ?: "",
                                isFall = isFall,
                                detectedAt = record["detected_at"]?.jsonPrimitive?.content ?: ""
                            )
                            onFallDetected(fall)
                        }
                    }
                }
                .catch { error ->
                    Log.e(TAG, "Error in fall detection stream", error)
                }
                .launchIn(scope)

            // Subscribe to the channel
            channel.subscribe()
            Log.d(TAG, "Successfully subscribed to falls channel")

        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to falls", e)
            throw e
        }
    }

    /**
     * Unsubscribe from all channels
     */
    suspend fun unsubscribe() {
        try {
            supabaseClient.realtime.removeAllChannels()
            Log.d(TAG, "Unsubscribed from all channels")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing", e)
        }
    }

    /**
     * Get user ID from session
     */
    suspend fun getUserId(): String? {
        return sessionManager.userId.first()
    }

    /**
     * Get auth token from session
     */
    suspend fun getAuthToken(): String? {
        return sessionManager.authToken.first()
    }

    /**
     * Close the Supabase client
     */
    suspend fun close() {
        try {
            supabaseClient.close()
            Log.d(TAG, "Supabase client closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Supabase client", e)
        }
    }
}
