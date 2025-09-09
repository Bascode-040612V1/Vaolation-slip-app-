package com.aics.violationapp.data.model

data class User(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "guard",
    val rfid: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "guard",
    val rfid: String? = null
)

data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
)
