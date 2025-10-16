package com.example.foodapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodapp.databinding.FragmentProfileBinding
import com.example.foodapp.utils.TokenManager

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    
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
        
        displayUserInfo()
    }
    
    private fun displayUserInfo() {
        binding.nameTextView.text = tokenManager.getUserName() ?: "Shipper"
        binding.emailTextView.text = tokenManager.getUserEmail() ?: ""
        binding.phoneTextView.text = tokenManager.getUserPhone() ?: ""
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
