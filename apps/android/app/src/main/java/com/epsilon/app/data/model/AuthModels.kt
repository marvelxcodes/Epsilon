package com.epsilon.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val redirect: Boolean? = null,
    val token: String? = null,
    val user: User? = null,
    // Error fields that might be present in error responses
    val error: String? = null,
    val message: String? = null,
    val statusCode: Int? = null
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)
