package com.epsilon.app.data.api

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
    private val baseUrl = "http://192.168.3.128:3001/api/auth"
    
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
        return try {
            val response = client.post("$baseUrl/sign-in/email") {
                setBody(LoginRequest(email, password))
            }
            
            // Extract cookie from response headers if present
            val cookie = response.headers["Set-Cookie"]
            
            val authResponse: AuthResponse = response.body()
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUp(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/sign-up/email") {
                setBody(SignUpRequest(name, email, password))
            }
            
            // Extract cookie from response headers if present
            val cookie = response.headers["Set-Cookie"]
            
            val authResponse: AuthResponse = response.body()
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        client.close()
    }
}
