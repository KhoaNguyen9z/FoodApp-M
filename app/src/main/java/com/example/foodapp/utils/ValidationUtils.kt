package com.example.foodapp.utils

import android.util.Patterns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.validator.routines.EmailValidator
import java.net.InetAddress
import java.util.regex.Pattern

object ValidationUtils {
    
    /**
     * Kiểm tra mật khẩu theo yêu cầu:
     * - Tối thiểu 8 ký tự
     * - Ít nhất 1 chữ thường
     * - Ít nhất 1 chữ hoa
     * - Ít nhất 1 số
     * - Ít nhất 1 ký tự đặc biệt
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult(false, "Vui lòng nhập mật khẩu")
        }
        
        if (password.length < 8) {
            return ValidationResult(false, "Mật khẩu phải có ít nhất 8 ký tự")
        }
        
        // Kiểm tra chữ thường
        if (!password.matches(Regex(".*[a-z].*"))) {
            return ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 chữ thường")
        }
        
        // Kiểm tra chữ hoa
        if (!password.matches(Regex(".*[A-Z].*"))) {
            return ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 chữ hoa")
        }
        
        // Kiểm tra số
        if (!password.matches(Regex(".*\\d.*"))) {
            return ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 số")
        }
        
        // Kiểm tra ký tự đặc biệt
        val specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")
        if (!specialCharPattern.matcher(password).find()) {
            return ValidationResult(false, "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (!@#$%^&*...)")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Kiểm tra email theo chuẩn RFC 5321
     */
    fun validateEmailFormat(email: String): ValidationResult {
        if (email.isEmpty()) {
            return ValidationResult(false, "Vui lòng nhập email")
        }
        
        // Sử dụng Apache Commons Validator để kiểm tra RFC 5321
        val validator = EmailValidator.getInstance(false, true)
        if (!validator.isValid(email)) {
            return ValidationResult(false, "Email không đúng định dạng RFC 5321")
        }
        
        // Kiểm tra thêm với Android Patterns
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, "Email không hợp lệ")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Kiểm tra DNS MX record của domain email
     * Phải chạy trên background thread
     * NOTE: Tạm thời disabled để tránh false positive với domain nội bộ
     */
    suspend fun validateEmailDNS(email: String): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val domain = email.substringAfter("@")
            
            if (domain.isEmpty()) {
                return@withContext ValidationResult(false, "Domain email không hợp lệ")
            }
            
            // Tạm thời skip DNS check - để server validate
            // Vì một số domain nội bộ/trường học có thể không public DNS
            // nhưng vẫn valid
            return@withContext ValidationResult(true, "")
            
            /* DNS Check disabled - uncomment nếu cần
            val hasValidDomain = checkDomainExists(domain)
            
            if (!hasValidDomain) {
                return@withContext ValidationResult(false, "Domain email không tồn tại")
            }
            
            return@withContext ValidationResult(true, "")
            */
            
        } catch (e: Exception) {
            // Nếu có lỗi DNS, vẫn cho phép đăng nhập (fallback)
            return@withContext ValidationResult(true, "")
        }
    }
    
    /**
     * Kiểm tra domain có tồn tại không bằng cách lookup DNS
     * Sử dụng InetAddress để kiểm tra A record
     */
    private fun checkDomainExists(domain: String): Boolean {
        return try {
            // Thử resolve domain name
            val addresses = InetAddress.getAllByName(domain)
            addresses.isNotEmpty()
        } catch (e: Exception) {
            // Nếu không resolve được, domain không tồn tại
            false
        }
    }
    
    /**
     * Validate email đầy đủ (cả format và DNS)
     */
    suspend fun validateEmailComplete(email: String): ValidationResult {
        // Kiểm tra format trước
        val formatResult = validateEmailFormat(email)
        if (!formatResult.isValid) {
            return formatResult
        }
        
        // Sau đó kiểm tra DNS
        return validateEmailDNS(email)
    }
}

/**
 * Data class để lưu kết quả validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)
