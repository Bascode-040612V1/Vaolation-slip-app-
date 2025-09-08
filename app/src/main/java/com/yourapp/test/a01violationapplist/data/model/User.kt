package com.yourapp.test.a01violationapplist.data.model

data class User(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "guard"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "guard"
)

data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
)