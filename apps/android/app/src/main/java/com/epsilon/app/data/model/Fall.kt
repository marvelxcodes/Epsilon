package com.epsilon.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fall(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("is_fall")
    val isFall: Boolean,
    @SerialName("detected_at")
    val detectedAt: String
)

@Serializable
data class FallRealtimePayload(
    val type: String,
    val table: String,
    val schema: String,
    val record: Fall?,
    @SerialName("old_record")
    val oldRecord: Fall?
)

