package com.example.foodapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.foodapp.R
import com.google.android.material.button.MaterialButton
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

class ErrorStateManager {
    
    enum class ErrorType {
        NO_INTERNET,
        SERVER_ERROR,
        TIMEOUT,
        UNKNOWN
    }
    
    companion object {
        
        /**
         * Kiểm tra kết nối mạng
         */
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        
        /**
         * Xác định loại lỗi từ Exception
         */
        fun getErrorType(exception: Throwable, context: Context): ErrorType {
            return when {
                !isNetworkAvailable(context) -> ErrorType.NO_INTERNET
                exception is UnknownHostException -> ErrorType.NO_INTERNET
                exception is SocketTimeoutException -> ErrorType.TIMEOUT
                exception is IOException -> ErrorType.SERVER_ERROR
                exception.message?.contains("HTTP 5") == true -> ErrorType.SERVER_ERROR
                exception.message?.contains("HTTP 4") == true -> ErrorType.SERVER_ERROR
                else -> ErrorType.UNKNOWN
            }
        }
        
        /**
         * Hiển thị error state trong ViewGroup
         */
        fun showErrorState(
            container: ViewGroup,
            errorType: ErrorType,
            onRetryClick: () -> Unit,
            onBackClick: (() -> Unit)? = null
        ): View {
            // Xóa error view cũ nếu có
            hideErrorState(container)
            
            // Inflate error layout
            val errorView = LayoutInflater.from(container.context)
                .inflate(R.layout.layout_error_state, container, false)
            
            val errorIcon = errorView.findViewById<ImageView>(R.id.errorIcon)
            val errorTitle = errorView.findViewById<TextView>(R.id.errorTitle)
            val errorMessage = errorView.findViewById<TextView>(R.id.errorMessage)
            val retryButton = errorView.findViewById<MaterialButton>(R.id.retryButton)
            val backButton = errorView.findViewById<MaterialButton>(R.id.backButton)
            
            // Cập nhật nội dung theo loại lỗi
            when (errorType) {
                ErrorType.NO_INTERNET -> {
                    errorIcon.setImageResource(R.drawable.ic_wifi_off_24)
                    errorTitle.text = "Không có kết nối mạng"
                    errorMessage.text = "Vui lòng kiểm tra kết nối Wi-Fi hoặc dữ liệu di động và thử lại"
                    retryButton.text = "🔄 Kết nối lại"
                }
                ErrorType.SERVER_ERROR -> {
                    errorIcon.setImageResource(R.drawable.ic_error_outline_24)
                    errorTitle.text = "Lỗi máy chủ"
                    errorMessage.text = "Máy chủ đang gặp sự cố. Vui lòng thử lại sau ít phút"
                    retryButton.text = "🔄 Thử lại"
                }
                ErrorType.TIMEOUT -> {
                    errorIcon.setImageResource(R.drawable.ic_error_outline_24)
                    errorTitle.text = "Kết nối quá chậm"
                    errorMessage.text = "Kết nối mạng không ổn định. Vui lòng thử lại"
                    retryButton.text = "🔄 Thử lại"
                }
                ErrorType.UNKNOWN -> {
                    errorIcon.setImageResource(R.drawable.ic_error_outline_24)
                    errorTitle.text = "Có lỗi xảy ra"
                    errorMessage.text = "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại"
                    retryButton.text = "🔄 Thử lại"
                }
            }
            
            // Set up click listeners
            retryButton.setOnClickListener { onRetryClick() }
            
            if (onBackClick != null) {
                backButton.visibility = View.VISIBLE
                backButton.setOnClickListener { onBackClick() }
            } else {
                backButton.visibility = View.GONE
            }
            
            // Add to container
            container.addView(errorView)
            errorView.tag = "error_state_view"
            
            return errorView
        }
        
        /**
         * Ẩn error state
         */
        fun hideErrorState(container: ViewGroup) {
            val errorView = container.findViewWithTag<View>("error_state_view")
            if (errorView != null) {
                container.removeView(errorView)
            }
        }
        
        /**
         * Kiểm tra xem có đang hiển thị error state không
         */
        fun isShowingErrorState(container: ViewGroup): Boolean {
            return container.findViewWithTag<View>("error_state_view") != null
        }
        
        /**
         * Show error với Exception
         */
        fun showError(
            container: ViewGroup,
            exception: Throwable,
            onRetryClick: () -> Unit,
            onBackClick: (() -> Unit)? = null
        ): View {
            val errorType = getErrorType(exception, container.context)
            return showErrorState(container, errorType, onRetryClick, onBackClick)
        }
    }
}