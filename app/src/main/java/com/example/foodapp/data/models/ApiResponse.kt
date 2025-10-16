package com.example.foodapp.data.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class LoginData(
    val user: User,
    val token: String
)
