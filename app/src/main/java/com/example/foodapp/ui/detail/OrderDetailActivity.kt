package com.example.foodapp.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.models.Order
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.ActivityOrderDetailBinding
import com.example.foodapp.ui.adapter.OrderItemAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OrderDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var viewModel: OrderDetailViewModel
    private lateinit var itemAdapter: OrderItemAdapter
    
    private var orderId: Int = -1
    private var fromAvailable: Boolean = false
    private var currentOrder: Order? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chi tiết đơn hàng"
        
        val order = intent.getSerializableExtra("ORDER_DATA") as? Order
        fromAvailable = intent.getBooleanExtra("FROM_AVAILABLE", false)
        
        val apiService = RetrofitClient.getApiService(this)
        val repository = ShipperRepository(apiService)
        viewModel = OrderDetailViewModel(repository)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        // Load order data
        order?.let {
            orderId = it.id
            viewModel.setOrder(it)
        }
    }
    
    private fun setupRecyclerView() {
        itemAdapter = OrderItemAdapter()
        binding.itemsRecyclerView.adapter = itemAdapter
    }
    
    private fun setupObservers() {
        viewModel.order.observe(this) { order ->
            currentOrder = order
            displayOrderDetails(order)
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.acceptButton.isEnabled = !isLoading
            binding.completeButton.isEnabled = !isLoading
        }
        
        viewModel.actionResult.observe(this) { result ->
            result.onSuccess { order ->
                Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show()
                displayOrderDetails(order)
            }
            
            result.onFailure { exception ->
                Toast.makeText(this, exception.message ?: "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun setupListeners() {
        binding.acceptButton.setOnClickListener {
            showConfirmDialog("Nhận đơn hàng", "Bạn có chắc chắn muốn nhận đơn hàng này?") {
                viewModel.acceptOrder(orderId)
            }
        }
        
        binding.completeButton.setOnClickListener {
            showConfirmDialog("Hoàn tất đơn hàng", "Xác nhận đã giao hàng thành công?") {
                viewModel.completeOrder(orderId)
            }
        }
    }
    
    private fun displayOrderDetails(order: Order) {
        binding.orderCodeTextView.text = order.ma_don_hang
        binding.statusTextView.text = order.trang_thai
        binding.dateTextView.text = order.ngay_tao ?: order.ngay_nhan ?: ""
        
        binding.customerNameTextView.text = order.khach_hang.ten
        binding.customerPhoneTextView.text = order.khach_hang.so_dien_thoai
        binding.addressTextView.text = order.dia_chi_giao
        
        if (!order.ghi_chu.isNullOrBlank()) {
            binding.noteLabel.visibility = View.VISIBLE
            binding.noteTextView.visibility = View.VISIBLE
            binding.noteTextView.text = order.ghi_chu
        } else {
            binding.noteLabel.visibility = View.GONE
            binding.noteTextView.visibility = View.GONE
        }
        
        itemAdapter.submitList(order.chi_tiet)
        
        binding.totalPriceTextView.text = order.tong_thanh_toan
        binding.paymentMethodTextView.text = order.payment_method
        binding.paymentStatusTextView.text = getPaymentStatusText(order.payment_status)
        
        // Set color for payment status
        binding.paymentStatusTextView.setTextColor(
            when (order.payment_status) {
                "paid" -> android.graphics.Color.parseColor("#4CAF50") // Green
                "pending" -> android.graphics.Color.parseColor("#FF9800") // Orange
                "failed" -> android.graphics.Color.parseColor("#F44336") // Red
                else -> android.graphics.Color.parseColor("#757575") // Gray
            }
        )
        
        // Show appropriate buttons based on status
        when (order.trang_thai) {
            "Đang chuẩn bị" -> {
                binding.acceptButton.visibility = View.VISIBLE
                binding.completeButton.visibility = View.GONE
            }
            "Đang giao" -> {
                binding.acceptButton.visibility = View.GONE
                binding.completeButton.visibility = View.VISIBLE
            }
            "Hoàn tất" -> {
                binding.acceptButton.visibility = View.GONE
                binding.completeButton.visibility = View.GONE
            }
            else -> {
                binding.acceptButton.visibility = View.GONE
                binding.completeButton.visibility = View.GONE
            }
        }
    }
    
    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Xác nhận") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun getPaymentStatusText(status: String): String {
        return when (status) {
            "paid" -> "Đã thanh toán"
            "pending" -> "Chưa thanh toán"
            "failed" -> "Thanh toán thất bại"
            else -> status
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
