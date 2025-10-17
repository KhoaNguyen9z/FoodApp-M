package com.example.foodapp.utils

import java.text.SimpleDateFormat
import java.util.*

object VietnameseDateFormatter {
    
    private val locale = Locale("vi", "VN")
    
    // Tên tháng tiếng Việt
    private val monthNames = arrayOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", 
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )
    
    // Tên ngày trong tuần tiếng Việt (viết tắt)
    private val dayNamesShort = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    
    // Tên ngày trong tuần tiếng Việt (đầy đủ)
    private val dayNamesFull = arrayOf(
        "Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", 
        "Thứ năm", "Thứ sáu", "Thứ bảy"
    )
    
    /**
     * Format: "Tháng 10, 2025"
     */
    fun formatMonthYear(date: Date): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"), locale)
        calendar.time = date
        
        val month = monthNames[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)
        
        return "$month, $year"
    }
    
    /**
     * Format: "17/10/2025"
     */
    fun formatDayMonthYear(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", locale)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        return sdf.format(date)
    }
    
    /**
     * Format: "Thứ ba, 17/10/2025"
     */
    fun formatFullDate(date: Date): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"), locale)
        calendar.time = date
        
        val dayOfWeek = dayNamesFull[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val formatted = formatDayMonthYear(date)
        
        return "$dayOfWeek, $formatted"
    }
    
    /**
     * Format: "yyyy-MM-dd" (cho API)
     */
    fun formatForApi(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", locale)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        return sdf.format(date)
    }
    
    /**
     * Lấy tên ngày trong tuần (viết tắt): CN, T2, T3...
     */
    fun getDayOfWeekShort(date: Date): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"), locale)
        calendar.time = date
        return dayNamesShort[calendar.get(Calendar.DAY_OF_WEEK) - 1]
    }
}
