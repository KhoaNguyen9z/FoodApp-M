package com.example.foodapp.ui.myorders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.FragmentMyOrdersBinding
import com.example.foodapp.ui.adapter.OrderAdapter
import com.example.foodapp.ui.detail.OrderDetailActivity
import com.google.android.material.tabs.TabLayout

class MyOrdersFragment : Fragment() {
    
    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MyOrdersViewModel
    private lateinit var orderAdapter: OrderAdapter
    
    private var currentStatus: String? = "Đang giao"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val apiService = RetrofitClient.getApiService(requireContext())
        val repository = ShipperRepository(apiService)
        viewModel = MyOrdersViewModel(repository)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        setupTabs()
        
        viewModel.loadMyOrders(currentStatus)
    }
    
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            onOrderClick = { order ->
                val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                    putExtra("ORDER_DATA", order)
                    putExtra("FROM_AVAILABLE", false)
                }
                startActivity(intent)
            },
            onAcceptOrderClick = null, // Không hiển thị nút nhận đơn trong tab "Đơn của tôi"
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
            viewModel.loadMyOrders(currentStatus)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentStatus = when (tab?.position) {
                    0 -> "Đang giao"
                    1 -> "Hoàn tất"
                    2 -> null
                    else -> null
                }
                viewModel.loadMyOrders(currentStatus)
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
    
    override fun onResume() {
        super.onResume()
        viewModel.loadMyOrders(currentStatus)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
