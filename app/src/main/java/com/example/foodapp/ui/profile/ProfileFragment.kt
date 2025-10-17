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
import com.example.foodapp.utils.showError
import com.example.foodapp.utils.hideError
import com.example.foodapp.utils.isNetworkAvailable
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
        // Kiểm tra binding trước khi sử dụng
        if (_binding == null) return
        
        binding.nameTextView.text = tokenManager.getUserName() ?: "Shipper"
        binding.emailTextView.text = tokenManager.getUserEmail() ?: ""
        binding.phoneTextView.text = tokenManager.getUserPhone() ?: ""
    }
    
    private fun setupLogoutButton() {
        // Kiểm tra binding trước khi sử dụng
        if (_binding == null) return
        
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
        // Sử dụng viewLifecycleOwner.lifecycleScope thay vì lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Kiểm tra binding trước khi sử dụng
                if (_binding == null) return@launch
                
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
                // Kiểm tra context vẫn còn
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Lỗi đăng xuất: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun loadStatistics() {
        // Kiểm tra binding trước khi sử dụng
        if (_binding == null) return
        
        // Kiểm tra kết nối mạng
        if (!isNetworkAvailable()) {
            // Hiển thị 0 và error state nếu cần
            if (_binding != null) {
                binding.completedCountTextView.text = "0"
                binding.cancelledCountTextView.text = "0"
                binding.expiredCountTextView.text = "0"
            }
            return
        }
        
        // Sử dụng viewLifecycleOwner.lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Kiểm tra binding trước mỗi lần sử dụng
                if (_binding == null) return@launch
                
                // Ẩn error state nếu có
                binding.root.hideError()
                
                // Load tất cả đơn hàng để thống kê
                val completedResult = repository.getMyOrders("Hoàn tất")
                val cancelledResult = repository.getMyOrders("Bị hủy")
                val expiredResult = repository.getMyOrders("Quá hạn")
                
                // Kiểm tra binding trước khi cập nhật UI
                if (_binding == null) return@launch
                
                completedResult.onSuccess { orders ->
                    if (_binding != null) {
                        binding.completedCountTextView.text = orders.size.toString()
                    }
                }
                
                cancelledResult.onSuccess { orders ->
                    if (_binding != null) {
                        binding.cancelledCountTextView.text = orders.size.toString()
                    }
                }
                
                expiredResult.onSuccess { orders ->
                    if (_binding != null) {
                        binding.expiredCountTextView.text = orders.size.toString()
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "Error loading statistics: ${e.message}")
                
                // Kiểm tra binding trước khi cập nhật UI
                if (_binding != null) {
                    binding.completedCountTextView.text = "0"
                    binding.cancelledCountTextView.text = "0"
                    binding.expiredCountTextView.text = "0"
                }
                
                // Có thể hiển thị error state nếu muốn
                // if (_binding != null) {
                //     binding.root.showError(e) { loadStatistics() }
                // }
            }
        }
    }
    
    override fun onDestroyView() {
        // Cleanup error state trước khi destroy
        if (_binding != null) {
            binding.root.hideError()
        }
        
        super.onDestroyView()
        _binding = null
    }
}
