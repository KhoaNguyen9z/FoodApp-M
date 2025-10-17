package com.example.foodapp.utils

/**
 * Test cases và ví dụ về validation
 * Sử dụng trong development/testing
 */
object ValidationTestCases {
    
    // ========== PASSWORD TEST CASES ==========
    
    val validPasswords = listOf(
        "Password123!",
        "MyP@ssw0rd",
        "Abc123!@#",
        "SecurePass1$",
        "Test@1234",
        "Admin#2024"
    )
    
    val invalidPasswords = mapOf(
        "password" to "Thiếu chữ hoa, số, ký tự đặc biệt",
        "PASSWORD123!" to "Thiếu chữ thường",
        "Password123" to "Thiếu ký tự đặc biệt",
        "Pass1!" to "Quá ngắn (< 8 ký tự)",
        "password123" to "Thiếu chữ hoa và ký tự đặc biệt",
        "Passw0rd" to "Thiếu ký tự đặc biệt",
        "12345678!" to "Thiếu chữ hoa và chữ thường",
        "ABCDEFGH!" to "Thiếu chữ thường và số"
    )
    
    // ========== EMAIL TEST CASES ==========
    
    val validEmails = listOf(
        "user@gmail.com",
        "test.user@company.vn",
        "admin+tag@domain.edu",
        "john.doe@example.co.uk",
        "contact@my-company.com",
        "info@subdomain.example.org"
    )
    
    val invalidEmails = mapOf(
        "invalid@" to "Thiếu domain",
        "@domain.com" to "Thiếu local part",
        "user@" to "Thiếu domain",
        "user" to "Không có @",
        "user @domain.com" to "Có khoảng trắng",
        "user@domain" to "Domain không hợp lệ (cần có TLD)",
        "" to "Email rỗng"
    )
    
    val emailsWithInvalidDNS = listOf(
        "user@fake-domain-xyz-12345.com",
        "test@nonexistent-email-domain.org",
        "admin@this-domain-does-not-exist.net"
    )
    
    // ========== HELPER FUNCTIONS ==========
    
    /**
     * Test tất cả password cases
     */
    fun testPasswordValidation(): List<Pair<String, ValidationResult>> {
        val results = mutableListOf<Pair<String, ValidationResult>>()
        
        println("===== TESTING VALID PASSWORDS =====")
        validPasswords.forEach { password ->
            val result = ValidationUtils.validatePassword(password)
            results.add(password to result)
            println("$password: ${if (result.isValid) "✅ PASS" else "❌ FAIL - ${result.errorMessage}"}")
        }
        
        println("\n===== TESTING INVALID PASSWORDS =====")
        invalidPasswords.forEach { (password, reason) ->
            val result = ValidationUtils.validatePassword(password)
            results.add(password to result)
            println("$password: ${if (!result.isValid) "✅ FAIL (expected)" else "❌ PASS (unexpected)"}")
            println("   Expected: $reason")
            println("   Got: ${result.errorMessage}")
        }
        
        return results
    }
    
    /**
     * Test email format validation
     */
    fun testEmailFormatValidation(): List<Pair<String, ValidationResult>> {
        val results = mutableListOf<Pair<String, ValidationResult>>()
        
        println("===== TESTING VALID EMAILS =====")
        validEmails.forEach { email ->
            val result = ValidationUtils.validateEmailFormat(email)
            results.add(email to result)
            println("$email: ${if (result.isValid) "✅ PASS" else "❌ FAIL - ${result.errorMessage}"}")
        }
        
        println("\n===== TESTING INVALID EMAILS =====")
        invalidEmails.forEach { (email, reason) ->
            val result = ValidationUtils.validateEmailFormat(email)
            results.add(email to result)
            println("$email: ${if (!result.isValid) "✅ FAIL (expected)" else "❌ PASS (unexpected)"}")
            println("   Expected: $reason")
            println("   Got: ${result.errorMessage}")
        }
        
        return results
    }
    
    /**
     * Print summary của validation requirements
     */
    fun printValidationRequirements() {
        println("""
            ╔════════════════════════════════════════════════════════════╗
            ║           VALIDATION REQUIREMENTS SUMMARY                  ║
            ╠════════════════════════════════════════════════════════════╣
            ║ PASSWORD REQUIREMENTS:                                     ║
            ║ • Minimum 8 characters                                     ║
            ║ • At least 1 lowercase letter (a-z)                        ║
            ║ • At least 1 uppercase letter (A-Z)                        ║
            ║ • At least 1 digit (0-9)                                   ║
            ║ • At least 1 special character (!@#$%^&*()_+-=[]{}...     ║
            ╠════════════════════════════════════════════════════════════╣
            ║ EMAIL REQUIREMENTS:                                        ║
            ║ • RFC 5321 compliant format                                ║
            ║ • Valid domain with DNS record                             ║
            ║ • Standard email pattern (user@domain.tld)                 ║
            ╚════════════════════════════════════════════════════════════╝
        """.trimIndent())
    }
}
