package com.example.foodapp.data.models

import java.io.Serializable

data class OrderItem(
    val ten_mon: String,
    val so_luong: Int,
    val don_gia: String,
    val thanh_tien: String
) : Serializable
