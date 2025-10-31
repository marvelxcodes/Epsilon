package com.epsilon.app.data.api

import android.util.Log
import com.epsilon.app.BuildConfig
import com.epsilon.app.data.model.CreateMedicationRequest
import com.epsilon.app.data.model.Medication
import com.epsilon.app.data.model.MedicationResponse
import com.epsilon.app.data.model.UpdateMedicationRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MedicationApiClient(private val sessionToken: String) {
    // Use BuildConfig for dynamic base URL based on build type
    private val baseUrl = "${BuildConfig.BACKEND_URL}/api/medicine"
    
    companion object {
        private const val TAG = "MedicationApiClient"
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
            // Send the session token as a proper cookie
            header("Cookie", "better-auth.session_token=$sessionToken")
        }
    }
    
    suspend fun getAllMedications(activeOnly: Boolean = false): Result<List<Medication>> {
        return try {
            val url = if (activeOnly) {
                "$baseUrl?activeOnly=true"
            } else {
                baseUrl
            }
            
            val response: MedicationResponse = client.get(url).body()
            val medications = response.medicines ?: emptyList()
            
            Log.d(TAG, "Fetched ${medications.size} medications")
            Result.success(medications)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching medications", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMedication(id: String): Result<Medication> {
        return try {
            val response: MedicationResponse = client.get("$baseUrl/$id").body()
            val medication = response.medicine
                ?: return Result.failure(Exception("Medication not found"))
            
            Log.d(TAG, "Fetched medication: ${medication.name}")
            Result.success(medication)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching medication", e)
            Result.failure(e)
        }
    }
    
    suspend fun createMedication(request: CreateMedicationRequest): Result<Medication> {
        return try {
            val response: MedicationResponse = client.post(baseUrl) {
                setBody(request)
            }.body()
            
            val medication = response.medicine
                ?: return Result.failure(Exception("Failed to create medication"))
            
            Log.d(TAG, "Created medication: ${medication.name}")
            Result.success(medication)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating medication", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateMedication(
        id: String,
        request: UpdateMedicationRequest
    ): Result<Medication> {
        return try {
            val response: MedicationResponse = client.put("$baseUrl/$id") {
                setBody(request)
            }.body()
            
            val medication = response.medicine
                ?: return Result.failure(Exception("Failed to update medication"))
            
            Log.d(TAG, "Updated medication: ${medication.name}")
            Result.success(medication)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating medication", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteMedication(id: String): Result<Unit> {
        return try {
            client.delete("$baseUrl/$id")
            Log.d(TAG, "Deleted medication: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting medication", e)
            Result.failure(e)
        }
    }
}
