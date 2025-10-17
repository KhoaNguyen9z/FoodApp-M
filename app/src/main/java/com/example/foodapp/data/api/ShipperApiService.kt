package com.example.foodapp.data.api

import com.example.foodapp.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ShipperApiService {
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>
    
    @GET("orders/available")
    suspend fun getAvailableOrders(): Response<ApiResponse<List<Order>>>
    
    @POST("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") orderId: Int): Response<ApiResponse<Order>>
    
    @POST("orders/{id}/complete")
    suspend fun completeOrder(@Path("id") orderId: Int): Response<ApiResponse<Order>>
    
    @POST("orders/{id}/update-payment")
    suspend fun updatePaymentStatus(
        @Path("id") orderId: Int,
        @Body request: UpdatePaymentRequest
    ): Response<ApiResponse<Order>>
    
    @GET("orders/my-orders")
    suspend fun getMyOrders(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<Order>>>
    
    @POST("logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}
