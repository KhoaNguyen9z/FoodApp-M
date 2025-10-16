package com.example.foodapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.data.models.Order
import com.example.foodapp.databinding.ItemOrderBinding

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit,
    private val onAcceptOrderClick: ((Order) -> Unit)? = null,
    private val onViewMapClick: ((Order) -> Unit)? = null
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding, onOrderClick, onAcceptOrderClick, onViewMapClick)
    }
    
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class OrderViewHolder(
        private val binding: ItemOrderBinding,
        private val onOrderClick: (Order) -> Unit,
        private val onAcceptOrderClick: ((Order) -> Unit)? = null,
        private val onViewMapClick: ((Order) -> Unit)? = null
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.orderCodeTextView.text = order.ma_don_hang
            binding.statusTextView.text = order.trang_thai
            binding.customerNameTextView.text = order.khach_hang.ten
            binding.customerPhoneTextView.text = order.khach_hang.so_dien_thoai
            binding.addressTextView.text = order.dia_chi_giao
            binding.totalPriceTextView.text = order.tong_thanh_toan
            binding.paymentMethodTextView.text = order.payment_method
            binding.paymentStatusTextView.text = getPaymentStatusText(order.payment_status)
            
            // Set color based on payment status
            binding.paymentStatusTextView.setTextColor(
                when (order.payment_status) {
                    "paid" -> android.graphics.Color.parseColor("#4CAF50") // Green
                    "pending" -> android.graphics.Color.parseColor("#FF9800") // Orange
                    "failed" -> android.graphics.Color.parseColor("#F44336") // Red
                    else -> android.graphics.Color.parseColor("#757575") // Gray
                }
            )
            
            // Show/hide accept button based on order status and callback availability
            if (onAcceptOrderClick != null && order.trang_thai == "Đang chuẩn bị") {
                binding.acceptOrderButton.visibility = android.view.View.VISIBLE
                binding.acceptOrderButton.setOnClickListener {
                    onAcceptOrderClick.invoke(order)
                }
            } else {
                binding.acceptOrderButton.visibility = android.view.View.GONE
            }
            
            // Map button is always visible if callback is provided and order has address
            if (onViewMapClick != null && order.dia_chi_giao.isNotBlank()) {
                binding.viewMapButton.visibility = android.view.View.VISIBLE
                binding.viewMapButton.setOnClickListener {
                    onViewMapClick.invoke(order)
                }
            } else {
                binding.viewMapButton.visibility = android.view.View.GONE
            }
            
            binding.root.setOnClickListener {
                onOrderClick(order)
            }
        }
        
        private fun getPaymentStatusText(status: String): String {
            return when (status) {
                "paid" -> "Đã thanh toán"
                "pending" -> "Chưa thanh toán"
                "failed" -> "Thanh toán thất bại"
                else -> status
            }
        }
    }
    
    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
