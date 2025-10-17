package com.example.foodapp.data.models

data class UpdatePaymentRequest(
    val payment_status: String // "paid", "pending", "failed"
)
