package com.epsilon.app.data.api

import android.util.Log
import com.epsilon.app.BuildConfig
import com.epsilon.app.data.model.AuthResponse
import com.epsilon.app.data.model.LoginRequest
import com.epsilon.app.data.model.SignUpRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AuthApiClient {
    private val baseUrl = "${BuildConfig.BACKEND_URL}/api/auth"
    
    companion object {
        private const val TAG = "AuthApiClient"
    }
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<AuthResponse> {
        Log.w(email,  baseUrl)
        return try {
            val response = client.post("$baseUrl/sign-in/email") {
                setBody(LoginRequest(email, password))
            }

            
            val authResponse: AuthResponse = response.body()

            val sessionToken = authResponse.token

            if (sessionToken.isNullOrEmpty()) {
                Log.e(TAG, "No session token found in response")
                return Result.failure(Exception("Authentication failed: No session token received"))
            }

            // Add the extracted token to the response
            val responseWithToken = authResponse.copy(token = sessionToken)
            
            Log.d(TAG, "Sign in successful, token extracted")
            Result.success(responseWithToken)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in error", e)
            Result.failure(e)
        }
    }
    
    suspend fun signUp(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/sign-up/email") {
                setBody(SignUpRequest(name, email, password))
            }
            
            // Extract session token from Set-Cookie header
            val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
            val sessionToken = extractSessionToken(cookies)
            
            if (sessionToken.isNullOrEmpty()) {
                Log.e(TAG, "No session token found in response")
                return Result.failure(Exception("Registration failed: No session token received"))
            }
            
            val authResponse: AuthResponse = response.body()
            
            // Add the extracted token to the response
            val responseWithToken = authResponse.copy(token = sessionToken)
            
            Log.d(TAG, "Sign up successful, token extracted")
            Result.success(responseWithToken)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Extract session token from Set-Cookie headers
     * Looks for better-auth.session_token cookie
     */
    private fun extractSessionToken(cookies: List<String>): String? {
        for (cookie in cookies) {
            if (cookie.startsWith("better-auth.session_token=")) {
                // Extract the token value before the first semicolon
                val tokenStart = cookie.indexOf("=") + 1
                val tokenEnd = cookie.indexOf(";").takeIf { it > 0 } ?: cookie.length
                return cookie.substring(tokenStart, tokenEnd).trim()
            }
        }
        return null
    }
    
    fun close() {
        client.close()
    }
}
