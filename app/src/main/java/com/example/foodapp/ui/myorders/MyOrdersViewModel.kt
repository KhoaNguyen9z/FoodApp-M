package com.example.foodapp.ui.myorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.models.Order
import com.example.foodapp.data.repository.ShipperRepository
import kotlinx.coroutines.launch

class MyOrdersViewModel(private val repository: ShipperRepository) : ViewModel() {
    
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _completeSuccess = MutableLiveData<Order?>()
    val completeSuccess: LiveData<Order?> = _completeSuccess
    
    fun loadMyOrders(status: String? = null, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                android.util.Log.d("MyOrdersViewModel", "Loading orders with: status=$status, startDate=$startDate, endDate=$endDate")
                
                val result = repository.getMyOrders(status, startDate, endDate)
                result.onSuccess { orderList ->
                    android.util.Log.d("MyOrdersViewModel", "Received ${orderList.size} orders")
                    orderList.forEach { order ->
                        android.util.Log.d("MyOrdersViewModel", "Order: ${order.ma_don_hang}, Status: ${order.trang_thai}, Created: ${order.ngay_tao}")
                    }
                    _orders.value = orderList
                }
                result.onFailure { exception ->
                    android.util.Log.e("MyOrdersViewModel", "Error loading orders: ${exception.message}")
                    _error.value = exception.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun completeOrder(orderId: Int, currentPaymentStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Kiểm tra nếu đã thanh toán (hỗ trợ cả "paid" và "Đã thanh toán")
                val isPaid = currentPaymentStatus.lowercase() == "paid" || 
                             currentPaymentStatus == "Đã thanh toán"
                
                // Chỉ cập nhật trạng thái thanh toán nếu CHƯA thanh toán
                if (!isPaid) {
                    val paymentResult = repository.updatePaymentStatus(orderId, "paid")
                    
                    if (paymentResult.isFailure) {
                        _error.value = "Không thể cập nhật trạng thái thanh toán: ${paymentResult.exceptionOrNull()?.message}"
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                // Hoàn tất đơn hàng (luôn thực hiện)
                val completeResult = repository.completeOrder(orderId)
                completeResult.onSuccess { order ->
                    _completeSuccess.value = order
                }
                completeResult.onFailure { exception ->
                    _error.value = exception.message
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
