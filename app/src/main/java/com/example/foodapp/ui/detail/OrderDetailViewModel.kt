package com.example.foodapp.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.models.Order
import com.example.foodapp.data.repository.ShipperRepository
import kotlinx.coroutines.launch

class OrderDetailViewModel(private val repository: ShipperRepository) : ViewModel() {
    
    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order> = _order
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _actionResult = MutableLiveData<Result<Order>>()
    val actionResult: LiveData<Result<Order>> = _actionResult
    
    fun setOrder(order: Order) {
        _order.value = order
    }
    
    fun acceptOrder(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.acceptOrder(orderId)
                _actionResult.value = result
                result.onSuccess { updatedOrder ->
                    _order.value = updatedOrder
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun completeOrder(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.completeOrder(orderId)
                _actionResult.value = result
                result.onSuccess { updatedOrder ->
                    _order.value = updatedOrder
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
