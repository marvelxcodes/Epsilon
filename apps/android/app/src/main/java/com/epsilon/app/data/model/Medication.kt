package com.epsilon.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Medication(
    val id: String,
    val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String, // e.g., "daily", "twice daily", "weekly"
    val time: String, // e.g., "08:00", "14:00,20:00" for multiple times
    val startDate: String,
    val endDate: String? = null,
    val notes: String? = null,
    val isActive: String = "true",
    val reminderEnabled: String = "true",
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateMedicationRequest(
    val name: String,
    val dosage: String,
    val frequency: String,
    val time: String,
    val startDate: String,
    val endDate: String? = null,
    val notes: String? = null,
    val reminderEnabled: Boolean = true
)

@Serializable
data class UpdateMedicationRequest(
    val name: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val time: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val notes: String? = null,
    val isActive: Boolean? = null,
    val reminderEnabled: Boolean? = null
)

@Serializable
data class MedicationResponse(
    val medicine: Medication? = null,
    val medicines: List<Medication>? = null,
    val message: String? = null
)
