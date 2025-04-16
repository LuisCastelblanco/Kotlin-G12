package com.example.explorandes.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.explorandes.R

class NotificationsFragment : Fragment() {
    
    private lateinit var backButton: ImageView
    private lateinit var emailSwitch: SwitchCompat
    private lateinit var notificationsSwitch: SwitchCompat
    private lateinit var popUpsSwitch: SwitchCompat
    private lateinit var cancelButton: Button
    private lateinit var applyButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        
        // Initialize views
        backButton = view.findViewById(R.id.backButton)
        emailSwitch = view.findViewById(R.id.emailSwitch)
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch)
        popUpsSwitch = view.findViewById(R.id.popUpsSwitch)
        cancelButton = view.findViewById(R.id.cancelButton)
        applyButton = view.findViewById(R.id.applyButton)
        
        setupUI()
        return view
    }
    
    private fun setupUI() {
        // Set click listeners
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        applyButton.setOnClickListener {
            applyNotificationSettings()
        }
    }
    
    private fun applyNotificationSettings() {
        // In a real app, this would update the user's notification preferences
        val emailEnabled = emailSwitch.isChecked
        val notificationsEnabled = notificationsSwitch.isChecked
        val popUpsEnabled = popUpsSwitch.isChecked
        
        Toast.makeText(requireContext(), "Notification settings updated", Toast.LENGTH_SHORT).show()
        
        // Return to account screen
        parentFragmentManager.popBackStack()
    }
}