package com.example.foodapp.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Order(
    val id: Int,
    val ma_don_hang: String,
    val khach_hang: Customer,
    val dia_chi_giao: String,
    val tong_thanh_toan: String,
    val tong_thanh_toan_raw: String? = null,
    val payment_method: String,
    val payment_status: String,
    val ghi_chu: String? = null,
    val trang_thai: String,
    @SerializedName("ngay_tao", alternate = ["created_at", "createdAt", "ngayTao"])
    val ngay_tao: String? = null,
    @SerializedName("ngay_nhan", alternate = ["accepted_at", "acceptedAt", "ngayNhan"])
    val ngay_nhan: String? = null,
    val chi_tiet: List<OrderItem>,
    val shipper_id: Int? = null
) : Serializable
