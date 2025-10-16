package com.example.foodapp.ui.available

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.models.Order
import com.example.foodapp.data.repository.ShipperRepository
import kotlinx.coroutines.launch

class AvailableOrdersViewModel(private val repository: ShipperRepository) : ViewModel() {
    
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getAvailableOrders()
                result.onSuccess { orderList ->
                    _orders.value = orderList
                }
                result.onFailure { exception ->
                    _error.value = exception.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
