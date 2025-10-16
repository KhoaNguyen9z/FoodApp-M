package com.example.foodapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.ActivityLoginBinding
import com.example.foodapp.ui.main.MainActivity
import com.example.foodapp.utils.TokenManager

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        tokenManager = TokenManager(this)
        
        // Check if already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        val apiService = RetrofitClient.getApiService(this)
        val repository = ShipperRepository(apiService)
        viewModel = LoginViewModel(repository)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loginButton.isEnabled = !isLoading
        }
        
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { loginData ->
                // Save token and user info
                tokenManager.saveToken(loginData.token)
                tokenManager.saveUserInfo(
                    loginData.user.id,
                    loginData.user.getFullName(),
                    loginData.user.email,
                    loginData.user.so_dien_thoai
                )
                
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            
            result.onFailure { exception ->
                Toast.makeText(this, exception.message ?: "Đăng nhập thất bại", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(email, password)
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
