package com.example.foodapp.ui.myorders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.foodapp.data.api.RetrofitClient
import com.example.foodapp.data.repository.ShipperRepository
import com.example.foodapp.databinding.FragmentMyOrdersBinding
import com.example.foodapp.ui.adapter.OrderAdapter
import com.example.foodapp.ui.detail.OrderDetailActivity
import com.example.foodapp.utils.showError
import com.example.foodapp.utils.hideError
import com.example.foodapp.utils.isNetworkAvailable
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MyOrdersFragment : Fragment() {
    
    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MyOrdersViewModel
    private lateinit var repository: ShipperRepository
    private lateinit var orderAdapter: OrderAdapter
    
    private var currentStatus: String? = "Đang giao"
    private var startDate: String? = null
    private var endDate: String? = null
    
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
        repository = ShipperRepository(apiService)
        viewModel = MyOrdersViewModel(repository)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        setupTabs()
        setupDateFilter()
        
        loadOrdersWithFilter()
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
            },
            onCompleteOrderClick = { order ->
                showCompleteOrderDialog(order)
            }
        )
        binding.ordersRecyclerView.adapter = orderAdapter
    }
    
    private fun setupObservers() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            // Ẩn error state khi có dữ liệu
            binding.root.hideError()
            
            orderAdapter.submitList(orders)
            binding.emptyTextView.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            binding.ordersRecyclerView.visibility = if (orders.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            // Ẩn error state khi đang loading
            if (isLoading) {
                binding.root.hideError()
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { errorMessage ->
                // Ẩn RecyclerView và EmptyView khi có lỗi
                binding.ordersRecyclerView.visibility = View.GONE
                binding.emptyTextView.visibility = View.GONE
                
                // Hiển thị error state thay vì Toast
                val exception = Exception(errorMessage)
                binding.root.showError(
                    exception = exception,
                    onRetryClick = {
                        loadOrdersWithFilter()
                    }
                )
            }
        }
        
        viewModel.completeSuccess.observe(viewLifecycleOwner) { order ->
            order?.let {
                val paymentMsg = when (order.payment_method) {
                    "COD" -> "\n💵 Đã thu tiền mặt: ${order.tong_thanh_toan}"
                    else -> ""
                }
                
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Thành công!")
                    .setMessage(
                        "Đơn hàng ${order.ma_don_hang} đã hoàn tất$paymentMsg\n\n" +
                        "✅ Trạng thái: Hoàn tất\n" +
                        "✅ Thanh toán: Đã thanh toán"
                    )
                    .setPositiveButton("OK") { _, _ ->
                        loadOrdersWithFilter()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadOrdersWithFilter()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentStatus = when (tab?.position) {
                    0 -> "Đang giao"
                    1 -> "Hoàn tất"
                    2 -> "Quá hạn"
                    3 -> "Bị hủy"
                    4 -> null // Tất cả
                    else -> null
                }
                
                // Hiện/ẩn date filter chỉ cho tab "Hoàn tất", "Quá hạn", "Bị hủy"
                if (tab?.position == 1 || tab?.position == 2 || tab?.position == 3) {
                    binding.dateFilterLayout.visibility = View.VISIBLE
                } else {
                    binding.dateFilterLayout.visibility = View.GONE
                    // Reset date filter khi chuyển tab
                    startDate = null
                    endDate = null
                    binding.chipGroupDateFilter.clearCheck()
                    binding.tvSelectedDateRange.visibility = View.GONE
                }
                
                loadOrdersWithFilter()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupDateFilter() {
        // Hôm nay
        binding.chipToday.setOnClickListener {
            setTodayFilter()
        }
        
        // 7 ngày qua
        binding.chip7Days.setOnClickListener {
            set7DaysFilter()
        }
        
        // Tháng này
        binding.chipThisMonth.setOnClickListener {
            setThisMonthFilter()
        }
        
        // Tùy chọn ngày
        binding.chipCustomDate.setOnClickListener {
            showDateRangePicker()
        }
    }
    
    private fun setTodayFilter() {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        // Đặt về đầu ngày (00:00:00)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        val today = sdf.format(calendar.time)
        
        startDate = today
        endDate = today
        
        android.util.Log.d("MyOrdersFragment", "Today filter: startDate=$startDate, endDate=$endDate")
        
        val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        updateDateRangeDisplay("Hôm nay (${displayFormat.format(calendar.time)})")
        loadOrdersWithFilter()
    }
    
    private fun set7DaysFilter() {
        val calendar = java.util.Calendar.getInstance()
        // Đặt về đầu ngày hiện tại
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val endDate = sdf.format(calendar.time)
        
        // Lùi 6 ngày (tổng 7 ngày bao gồm hôm nay)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -6)
        val startDate = sdf.format(calendar.time)
        
        this.startDate = startDate
        this.endDate = endDate
        
        val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 6) // Quay lại hôm nay để hiển thị
        val endDisplay = displayFormat.format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -6) // Quay lại ngày đầu
        val startDisplay = displayFormat.format(calendar.time)
        
        updateDateRangeDisplay("7 ngày qua ($startDisplay - $endDisplay)")
        loadOrdersWithFilter()
    }
    
    private fun setThisMonthFilter() {
        val calendar = java.util.Calendar.getInstance()
        // Đặt về đầu ngày hiện tại
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val endDate = sdf.format(calendar.time)
        
        // Đặt về ngày 1 của tháng
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val startDate = sdf.format(calendar.time)
        
        this.startDate = startDate
        this.endDate = endDate
        
        val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val startDisplay = displayFormat.format(calendar.time)
        // Quay lại hôm nay để hiển thị ngày cuối
        calendar.set(java.util.Calendar.DAY_OF_MONTH, java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH))
        val endDisplay = displayFormat.format(calendar.time)
        
        updateDateRangeDisplay("Tháng này ($startDisplay - $endDisplay)")
        loadOrdersWithFilter()
    }
    
    private fun showDateRangePicker() {
        // Sử dụng MaterialDatePicker với cấu hình compact
        val locale = java.util.Locale("vi", "VN")
        
        val constraintsBuilder = com.google.android.material.datepicker.CalendarConstraints.Builder()
        val today = com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds()
        constraintsBuilder.setEnd(today)
        
        val builder = com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Chọn khoảng thời gian")
        builder.setCalendarConstraints(constraintsBuilder.build())
        
        // Set theme để hiển thị compact hơn
        builder.setTheme(com.example.foodapp.R.style.MaterialCalendarDialog)
        
        val picker = builder.build()
        
        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first
            val endMillis = selection.second
            
            val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"), locale)
            
            calendar.timeInMillis = startMillis
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", locale)
            sdf.timeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            startDate = sdf.format(calendar.time)
            
            calendar.timeInMillis = endMillis
            endDate = sdf.format(calendar.time)
            
            val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", locale)
            displayFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            
            calendar.timeInMillis = startMillis
            val startDisplay = displayFormat.format(calendar.time)
            calendar.timeInMillis = endMillis
            val endDisplay = displayFormat.format(calendar.time)
            
            val displayText = "$startDisplay - $endDisplay"
            updateDateRangeDisplay(displayText)
            loadOrdersWithFilter()
        }
        
        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }
    
    private fun updateDateRangeDisplay(text: String) {
        binding.tvSelectedDateRange.text = text
        binding.tvSelectedDateRange.visibility = View.VISIBLE
    }
    
    private fun loadOrdersWithFilter() {
        android.util.Log.d("MyOrdersFragment", "Loading orders: status=$currentStatus, startDate=$startDate, endDate=$endDate")
        
        // Kiểm tra kết nối mạng trước khi gọi API
        if (!isNetworkAvailable()) {
            // Ẩn RecyclerView và EmptyView
            binding.ordersRecyclerView.visibility = View.GONE
            binding.emptyTextView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            
            // Hiển thị error state cho mất kết nối mạng
            binding.root.showError(
                exception = Exception("Không có kết nối mạng"),
                onRetryClick = {
                    loadOrdersWithFilter()
                }
            )
            return
        }
        
        // Xử lý riêng cho tab "Tất cả" (fallback nếu backend không support status=null)
        if (currentStatus == null) {
            loadAllOrdersWithFallback()
        } else {
            viewModel.loadMyOrders(currentStatus, startDate, endDate)
        }
    }
    
    private fun loadAllOrdersWithFallback() {
        android.util.Log.d("MyOrdersFragment", "Loading all orders with fallback method")
        
        // Thử gọi API với status=null trước
        viewModel.loadMyOrders(null, startDate, endDate)
        
        // Nếu không có kết quả sau 3 giây, dùng fallback
        binding.root.postDelayed({
            if (orderAdapter.itemCount == 0 && !viewModel.isLoading.value!!) {
                android.util.Log.d("MyOrdersFragment", "No data from status=null, using fallback")
                loadAllOrdersFallback()
            }
        }, 3000)
    }
    
    private fun loadAllOrdersFallback() {
        // Load từng loại đơn riêng rồi merge (fallback method)
        android.util.Log.d("MyOrdersFragment", "Using fallback: loading each status separately")
        
        lifecycleScope.launch {
            try {
                val allOrders = mutableListOf<com.example.foodapp.data.models.Order>()
                
                // Load từng status
                val statuses = listOf("Đang giao", "Hoàn tất", "Quá hạn", "Bị hủy")
                
                statuses.forEach { status ->
                    try {
                        val result = repository.getMyOrders(status, startDate, endDate)
                        result.onSuccess { orders ->
                            allOrders.addAll(orders)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MyOrdersFragment", "Error loading $status: ${e.message}")
                    }
                }
                
                // Cập nhật UI
                orderAdapter.submitList(allOrders)
                binding.emptyTextView.visibility = if (allOrders.isEmpty()) View.VISIBLE else View.GONE
                binding.ordersRecyclerView.visibility = if (allOrders.isNotEmpty()) View.VISIBLE else View.GONE
                
                android.util.Log.d("MyOrdersFragment", "Fallback loaded ${allOrders.size} orders total")
                
            } catch (e: Exception) {
                android.util.Log.e("MyOrdersFragment", "Fallback failed: ${e.message}")
                // Hiển thị error state
                binding.root.showError(
                    exception = e,
                    onRetryClick = { loadOrdersWithFilter() }
                )
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
    
    private fun showCompleteOrderDialog(order: com.example.foodapp.data.models.Order) {
        // Tạo custom dialog với Material Design
        val dialogView = layoutInflater.inflate(
            com.example.foodapp.R.layout.dialog_complete_order, 
            null
        )
        
        val tvOrderCode = dialogView.findViewById<android.widget.TextView>(com.example.foodapp.R.id.tvOrderCode)
        val tvAmount = dialogView.findViewById<android.widget.TextView>(com.example.foodapp.R.id.tvAmount)
        val tvPaymentStatus = dialogView.findViewById<android.widget.TextView>(com.example.foodapp.R.id.tvPaymentStatus)
        
        tvOrderCode.text = order.ma_don_hang
        tvAmount.text = order.tong_thanh_toan
        
        // Set payment status text and color
        val isPaid = order.payment_status.lowercase() == "paid" || 
                     order.payment_status == "Đã thanh toán"
        
        if (isPaid) {
            tvPaymentStatus.text = "Đã thanh toán"
            tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            tvPaymentStatus.text = "Chưa thanh toán"
            tvPaymentStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
        }
        
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("XÁC NHẬN") { _, _ ->
                viewModel.completeOrder(order.id, order.payment_status)
            }
            .setNegativeButton("HỦY", null)
            .create()
        
        dialog.show()
        
        // Tùy chỉnh màu nút
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            android.graphics.Color.parseColor("#4CAF50")
        )
    }
    
    override fun onResume() {
        super.onResume()
        loadOrdersWithFilter()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
