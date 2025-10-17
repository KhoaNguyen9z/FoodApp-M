package com.example.foodapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.ActivityLoginBinding
import com.example.foodapp.ui.main.MainActivity
import com.example.foodapp.utils.TokenManager
import com.example.foodapp.utils.ValidationUtils
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
                // Hide error card if showing
                hideErrorMessage()
                
                // Save token and user info
                tokenManager.saveToken(loginData.token)
                tokenManager.saveUserInfo(
                    loginData.user.id,
                    loginData.user.getFullName(),
                    loginData.user.email,
                    loginData.user.so_dien_thoai
                )
                
                // Navigate to main screen
                navigateToMain()
            }
            
            result.onFailure { exception ->
                // Phân loại lỗi và hiển thị thông báo chi tiết
                handleLoginError(exception)
            }
        }
    }
    
    /**
     * Xử lý và hiển thị lỗi đăng nhập chi tiết
     */
    private fun handleLoginError(exception: Throwable) {
        val errorMessage = when (exception) {
            // Lỗi HTTP 401 - Unauthorized (sai tài khoản/mật khẩu)
            is HttpException -> {
                when (exception.code()) {
                    401 -> "❌ Tài khoản hoặc mật khẩu không chính xác.\nVui lòng kiểm tra lại thông tin đăng nhập."
                    404 -> "❌ Tài khoản không tồn tại trong hệ thống.\nVui lòng kiểm tra lại email."
                    403 -> "❌ Tài khoản của bạn đã bị khóa.\nVui lòng liên hệ quản trị viên."
                    500, 502, 503 -> "🔧 Lỗi server (${exception.code()}).\nVui lòng thử lại sau ít phút."
                    else -> "🔧 Lỗi server (${exception.code()}).\nVui lòng thử lại sau."
                }
            }
            
            // Lỗi timeout
            is SocketTimeoutException -> {
                "⏱️ Kết nối quá chậm hoặc server không phản hồi.\nVui lòng kiểm tra kết nối mạng và thử lại."
            }
            
            // Lỗi không có kết nối internet
            is UnknownHostException -> {
                "📡 Không có kết nối internet.\nVui lòng kiểm tra kết nối mạng của bạn."
            }
            
            // Các lỗi khác
            else -> {
                val message = exception.message
                when {
                    message?.contains("401") == true || 
                    message?.contains("Unauthorized") == true ||
                    message?.contains("Invalid credentials") == true -> {
                        "❌ Tài khoản hoặc mật khẩu không chính xác.\nVui lòng kiểm tra lại thông tin đăng nhập."
                    }
                    message?.contains("network") == true ||
                    message?.contains("timeout") == true -> {
                        "📡 Lỗi kết nối mạng.\nVui lòng kiểm tra internet và thử lại."
                    }
                    else -> {
                        "🔧 Lỗi server: ${message ?: "Không xác định"}.\nVui lòng thử lại sau."
                    }
                }
            }
        }
        
        showErrorMessage(errorMessage)
    }
    
    /**
     * Hiển thị thông báo lỗi
     */
    private fun showErrorMessage(message: String) {
        binding.errorCard.visibility = View.VISIBLE
        binding.errorTextView.text = message
        
        // Scroll to show error message
        binding.root.post {
            binding.root.smoothScrollTo(0, binding.errorCard.top)
        }
    }
    
    /**
     * Ẩn thông báo lỗi
     */
    private fun hideErrorMessage() {
        binding.errorCard.visibility = View.GONE
    }
    
    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            
            // Clear previous errors
            binding.emailInputLayout.error = null
            binding.passwordInputLayout.error = null
            hideErrorMessage()
            
            // Validate inputs
            if (validateInputs(email, password)) {
                // Nếu validation pass, tiếp tục kiểm tra DNS và đăng nhập
                validateAndLogin(email, password)
            }
        }
        
        // Clear error khi user bắt đầu nhập email
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideErrorMessage()
                binding.emailInputLayout.error = null
            } else {
                val email = binding.emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    val result = ValidationUtils.validateEmailFormat(email)
                    if (!result.isValid) {
                        binding.emailInputLayout.error = result.errorMessage
                    } else {
                        binding.emailInputLayout.error = null
                    }
                }
            }
        }
        
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hideErrorMessage()
                binding.passwordInputLayout.error = null
            } else {
                val password = binding.passwordEditText.text.toString()
                if (password.isNotEmpty()) {
                    val result = ValidationUtils.validatePassword(password)
                    if (!result.isValid) {
                        binding.passwordInputLayout.error = result.errorMessage
                    } else {
                        binding.passwordInputLayout.error = null
                    }
                }
            }
        }
    }
    
    /**
     * Validate email format và password trước
     */
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        // Validate email format
        val emailResult = ValidationUtils.validateEmailFormat(email)
        if (!emailResult.isValid) {
            binding.emailInputLayout.error = emailResult.errorMessage
            isValid = false
        }
        
        // Validate password
        val passwordResult = ValidationUtils.validatePassword(password)
        if (!passwordResult.isValid) {
            binding.passwordInputLayout.error = passwordResult.errorMessage
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Validate DNS và thực hiện login
     */
    private fun validateAndLogin(email: String, password: String) {
        lifecycleScope.launch {
            // Hiển thị loading
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.isEnabled = false
            
            // Kiểm tra DNS MX record
            val dnsResult = ValidationUtils.validateEmailDNS(email)
            
            if (!dnsResult.isValid) {
                binding.emailInputLayout.error = dnsResult.errorMessage
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                return@launch
            }
            
            // Nếu tất cả validation pass, thực hiện login
            binding.emailInputLayout.error = null
            viewModel.login(email, password)
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
