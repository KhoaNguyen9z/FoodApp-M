package com.example.foodapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.models.LoginData
import com.example.foodapp.data.repository.ShipperRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: ShipperRepository) : ViewModel() {
    
    private val _loginResult = MutableLiveData<Result<LoginData>>()
    val loginResult: LiveData<Result<LoginData>> = _loginResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = Result.failure(Exception("Email và mật khẩu không được để trống"))
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.login(email, password)
                _loginResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
}
