package com.example.foodapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.example.foodapp.databinding.ActivityMainNewBinding
import com.example.foodapp.ui.available.AvailableOrdersFragment
import com.example.foodapp.ui.login.LoginActivity
import com.example.foodapp.ui.myorders.MyOrdersFragment
import com.example.foodapp.ui.profile.ProfileFragment
import com.example.foodapp.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainNewBinding
    private lateinit var tokenManager: TokenManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set locale tiếng Việt cho toàn app
        setLocale()
        
        binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        tokenManager = TokenManager(this)
        
        setSupportActionBar(binding.toolbar)
        
        setupBottomNavigation()
        
        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(AvailableOrdersFragment())
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_available_orders -> {
                    loadFragment(AvailableOrdersFragment())
                    binding.toolbar.title = "Đơn có thể nhận"
                    true
                }
                R.id.nav_my_orders -> {
                    loadFragment(MyOrdersFragment())
                    binding.toolbar.title = "Đơn của tôi"
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    binding.toolbar.title = "Tài khoản"
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragment, fragment)
            .commit()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                logout()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun logout() {
        tokenManager.clearAll()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    private fun setLocale() {
        val locale = java.util.Locale("vi", "VN")
        java.util.Locale.setDefault(locale)
        
        val config = resources.configuration
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
