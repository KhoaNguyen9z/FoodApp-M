package com.example.foodapp.data.models

data class User(
    val id: Int,
    val ho: String,
    val ten: String,
    val email: String,
    val so_dien_thoai: String,
    val vai_tro_id: Int
) {
    fun getFullName(): String = "$ho $ten"
}
