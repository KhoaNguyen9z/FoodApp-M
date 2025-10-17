package com.example.foodapp.utils

import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Extension functions để dễ dàng sử dụng ErrorStateManager
 */

/**
 * Hiển thị lỗi kết nối mạng
 */
fun ViewGroup.showNoInternetError(onRetryClick: () -> Unit, onBackClick: (() -> Unit)? = null) {
    ErrorStateManager.showErrorState(this, ErrorStateManager.ErrorType.NO_INTERNET, onRetryClick, onBackClick)
}

/**
 * Hiển thị lỗi server
 */
fun ViewGroup.showServerError(onRetryClick: () -> Unit, onBackClick: (() -> Unit)? = null) {
    ErrorStateManager.showErrorState(this, ErrorStateManager.ErrorType.SERVER_ERROR, onRetryClick, onBackClick)
}

/**
 * Hiển thị lỗi timeout
 */
fun ViewGroup.showTimeoutError(onRetryClick: () -> Unit, onBackClick: (() -> Unit)? = null) {
    ErrorStateManager.showErrorState(this, ErrorStateManager.ErrorType.TIMEOUT, onRetryClick, onBackClick)
}

/**
 * Hiển thị lỗi từ Exception
 */
fun ViewGroup.showError(exception: Throwable, onRetryClick: () -> Unit, onBackClick: (() -> Unit)? = null) {
    ErrorStateManager.showError(this, exception, onRetryClick, onBackClick)
}

/**
 * Ẩn error state
 */
fun ViewGroup.hideError() {
    ErrorStateManager.hideErrorState(this)
}

/**
 * Kiểm tra xem có đang hiển thị error không
 */
fun ViewGroup.isShowingError(): Boolean {
    return ErrorStateManager.isShowingErrorState(this)
}

/**
 * Extension cho Fragment
 */
fun Fragment.isNetworkAvailable(): Boolean {
    return ErrorStateManager.isNetworkAvailable(requireContext())
}