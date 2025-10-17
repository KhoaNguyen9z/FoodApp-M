package com.example.foodapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foodapp.ui.login.LoginActivity
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.FragmentProfileBinding
import com.example.foodapp.utils.TokenManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: ShipperRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tokenManager = TokenManager(requireContext())
        val apiService = RetrofitClient.getApiService(requireContext())
        repository = ShipperRepository(apiService)
        
        displayUserInfo()
        setupLogoutButton()
        loadStatistics()
    }
    
    private fun displayUserInfo() {
        binding.nameTextView.text = tokenManager.getUserName() ?: "Shipper"
        binding.emailTextView.text = tokenManager.getUserEmail() ?: ""
        binding.phoneTextView.text = tokenManager.getUserPhone() ?: ""
    }
    
    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Gọi API logout
                repository.logout()
                
                // Xóa token và thông tin user
                tokenManager.clearAll()
                
                Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                
                // Chuyển về màn hình đăng nhập
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi đăng xuất: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // Load tất cả đơn hàng để thống kê
                val completedResult = repository.getMyOrders("Hoàn tất")
                val cancelledResult = repository.getMyOrders("Đã hủy")
                val expiredResult = repository.getMyOrders("Quá hạn")
                
                completedResult.onSuccess { orders ->
                    binding.completedCountTextView.text = orders.size.toString()
                }
                
                cancelledResult.onSuccess { orders ->
                    binding.cancelledCountTextView.text = orders.size.toString()
                }
                
                expiredResult.onSuccess { orders ->
                    binding.expiredCountTextView.text = orders.size.toString()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Error loading statistics: ${e.message}")
                // Hiển thị 0 nếu có lỗi
                binding.completedCountTextView.text = "0"
                binding.cancelledCountTextView.text = "0"
                binding.expiredCountTextView.text = "0"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
