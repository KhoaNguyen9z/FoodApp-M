package com.example.foodapp.ui.available

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.FragmentAvailableOrdersBinding
import com.example.foodapp.ui.adapter.OrderAdapter
import com.example.foodapp.ui.detail.OrderDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AvailableOrdersFragment : Fragment() {
    
    private var _binding: FragmentAvailableOrdersBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AvailableOrdersViewModel
    private lateinit var orderAdapter: OrderAdapter
    
    // Auto-refresh handler
    private val autoRefreshHandler = Handler(Looper.getMainLooper())
    private val autoRefreshInterval = 10000L // 10 giây
    
    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            if (isAdded && isVisible) {
                // Auto-refresh: không hiển thị loading spinner
                viewModel.loadAvailableOrders(showLoading = false)
            }
            autoRefreshHandler.postDelayed(this, autoRefreshInterval)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailableOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val apiService = RetrofitClient.getApiService(requireContext())
        val repository = ShipperRepository(apiService)
        viewModel = AvailableOrdersViewModel(repository)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        
        // Load lần đầu
        viewModel.loadAvailableOrders()
        
        // Bắt đầu auto-refresh
        startAutoRefresh()
    }
    
    override fun onResume() {
        super.onResume()
        // Restart auto-refresh khi fragment visible
        startAutoRefresh()
    }
    
    override fun onPause() {
        super.onPause()
        // Stop auto-refresh khi fragment không visible
        stopAutoRefresh()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefresh()
        _binding = null
    }
    
    private fun startAutoRefresh() {
        stopAutoRefresh() // Stop trước để tránh duplicate
        autoRefreshHandler.postDelayed(autoRefreshRunnable, autoRefreshInterval)
    }
    
    private fun stopAutoRefresh() {
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable)
    }
    
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            onOrderClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                    putExtra("ORDER_DATA", order)
                    putExtra("FROM_AVAILABLE", true)
                }
                startActivity(intent)
            },
            onAcceptOrderClick = { order ->
                showAcceptOrderDialog(order)
            },
            onViewMapClick = { order ->
                openMapWithAddress(order.dia_chi_giao)
            }
        )
        binding.ordersRecyclerView.adapter = orderAdapter
    }
    
    private fun setupObservers() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            orderAdapter.submitList(orders)
            binding.emptyTextView.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Manual refresh: hiển thị loading spinner
            viewModel.loadAvailableOrders(showLoading = true)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun showAcceptOrderDialog(order: com.example.foodapp.data.models.Order) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nhận đơn hàng")
            .setMessage("Bạn có chắc chắn muốn nhận đơn hàng ${order.ma_don_hang}?")
            .setPositiveButton("Nhận đơn") { _, _ ->
                acceptOrder(order)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun acceptOrder(order: com.example.foodapp.data.models.Order) {
        val apiService = RetrofitClient.getApiService(requireContext())
        val repository = ShipperRepository(apiService)
        
        // Use a simple coroutine scope for this operation
        lifecycleScope.launch {
            try {
                val result = repository.acceptOrder(order.id)
                result.onSuccess {
                    // Hiển thị Snackbar trước
                    if (isAdded && view != null) {
                        Snackbar.make(
                            binding.root,
                            "✅ Nhận đơn hàng thành công!",
                            Snackbar.LENGTH_SHORT
                        ).setBackgroundTint(
                            requireContext().getColor(android.R.color.holo_green_dark)
                        ).setTextColor(
                            requireContext().getColor(android.R.color.white)
                        ).show()
                    }
                    
                    // Delay rồi chuyển tab
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        try {
                            if (isAdded) {
                                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                                    com.example.foodapp.R.id.bottomNavigationView
                                )?.selectedItemId = com.example.foodapp.R.id.nav_my_orders
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, 500)
                }
                result.onFailure { exception ->
                    val errorMsg = when {
                        exception.message?.contains("500") == true -> 
                            "Lỗi server: Có vấn đề với cơ sở dữ liệu. Vui lòng liên hệ admin."
                        exception.message?.contains("404") == true -> 
                            "Đơn hàng không tồn tại hoặc đã được nhận bởi shipper khác"
                        exception.message?.contains("400") == true -> 
                            "Đơn hàng này không thể nhận (trạng thái không phù hợp)"
                        else -> exception.message ?: "Có lỗi xảy ra"
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Có lỗi xảy ra: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun openMapWithAddress(address: String) {
        try {
            // Try to open Google Maps first
            val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(address)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback to any app that can handle the geo intent
                val generalMapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                if (generalMapIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(generalMapIntent)
                } else {
                    // Last resort: open address in web browser
                    val webIntent = Intent(Intent.ACTION_VIEW, 
                        android.net.Uri.parse("https://www.google.com/maps/search/${android.net.Uri.encode(address)}"))
                    startActivity(webIntent)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Không thể mở bản đồ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
}
