package com.example.foodapp.data.repository

import com.example.foodapp.data.api.ShipperApiService
import com.example.foodapp.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShipperRepository(private val apiService: ShipperApiService) {
    
    suspend fun login(email: String, password: String): Result<LoginData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Đăng nhập thất bại"))
                }
            } else {
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAvailableOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableOrders()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.data ?: emptyList())
                } else {
                    Result.failure(Exception(body?.message ?: "Không thể lấy danh sách đơn hàng"))
                }
            } else {
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun acceptOrder(orderId: Int): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.acceptOrder(orderId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Không thể nhận đơn hàng"))
                }
            } else {
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeOrder(orderId: Int): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.completeOrder(orderId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Không thể hoàn tất đơn hàng"))
                }
            } else {
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePaymentStatus(orderId: Int, paymentStatus: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePaymentStatus(orderId, UpdatePaymentRequest(paymentStatus))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Không thể cập nhật trạng thái thanh toán"))
                }
            } else {
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMyOrders(status: String? = null, startDate: String? = null, endDate: String? = null): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("ShipperRepository", "Calling API with status=$status, startDate=$startDate, endDate=$endDate")
            val response = apiService.getMyOrders(status, startDate, endDate)
            android.util.Log.d("ShipperRepository", "API Response Code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("ShipperRepository", "Response body success: ${body?.success}, data size: ${body?.data?.size}")
                
                if (body?.success == true) {
                    val orders = body.data ?: emptyList()
                    android.util.Log.d("ShipperRepository", "Parsed ${orders.size} orders")
                    orders.take(5).forEach { order ->
                        android.util.Log.d("ShipperRepository", "Order ${order.ma_don_hang}: ngay_tao=${order.ngay_tao}, ngay_nhan=${order.ngay_nhan}")
                    }
                    Result.success(orders)
                } else {
                    Result.failure(Exception(body?.message ?: "Không thể lấy danh sách đơn hàng"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ShipperRepository", "Error response: $errorBody")
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            android.util.Log.e("ShipperRepository", "JSON parsing error: ${e.message}", e)
            Result.failure(Exception("Lỗi phân tích dữ liệu từ server. Vui lòng thử lại sau."))
        } catch (e: Exception) {
            android.util.Log.e("ShipperRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Lỗi kết nối: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
