package com.example.foodapp.ui.myorders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.foodapp.databinding.BottomSheetDateRangePickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class DateRangePickerBottomSheet(
    private val onDateRangeSelected: (String, String, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDateRangePickerBinding? = null
    private val binding get() = _binding!!

    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

    private val locale = Locale("vi", "VN")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", locale)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

    init {
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        apiDateFormat.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDateRangePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupButtons()
    }

    private fun setupCalendar() {
        // Không sử dụng MaterialDatePicker fullscreen nữa
        // Thay vào đó, sử dụng MaterialCalendar trực tiếp trong bottom sheet
        
        val constraintsBuilder = CalendarConstraints.Builder()
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        constraintsBuilder.setEnd(today)

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("") // Ẩn title vì đã có trong bottom sheet
        builder.setCalendarConstraints(constraintsBuilder.build())

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            startDateMillis = selection.first
            endDateMillis = selection.second

            updateDateDisplay()
            binding.btnConfirm.isEnabled = true
            
            // Tự động đóng picker sau khi chọn xong
            if (picker.isAdded) {
                picker.dismiss()
            }
        }

        // Show picker nhúng trong fragment của bottom sheet thay vì fullscreen
        picker.show(childFragmentManager, "DATE_PICKER")
        
        // Ẩn dialog mặc định của picker, chỉ lấy calendar view
        picker.dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        picker.dialog?.setOnShowListener {
            // Embed picker vào container của bottom sheet
            try {
                val pickerView = picker.view
                if (pickerView != null && pickerView.parent == null) {
                    binding.calendarContainer.removeAllViews()
                    binding.calendarContainer.addView(pickerView)
                }
            } catch (e: Exception) {
                android.util.Log.e("DateRangePicker", "Error embedding picker: ${e.message}")
            }
        }
    }

    private fun setupButtons() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            startDateMillis?.let { start ->
                endDateMillis?.let { end ->
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"), locale)

                    calendar.timeInMillis = start
                    val startDate = apiDateFormat.format(calendar.time)
                    val startDisplay = dateFormat.format(calendar.time)

                    calendar.timeInMillis = end
                    val endDate = apiDateFormat.format(calendar.time)
                    val endDisplay = dateFormat.format(calendar.time)

                    val displayText = "$startDisplay - $endDisplay"

                    onDateRangeSelected(startDate, endDate, displayText)
                    dismiss()
                }
            }
        }
    }

    private fun updateDateDisplay() {
        startDateMillis?.let { start ->
            binding.tvStartDate.text = dateFormat.format(Date(start))
        }

        endDateMillis?.let { end ->
            binding.tvEndDate.text = dateFormat.format(Date(end))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
