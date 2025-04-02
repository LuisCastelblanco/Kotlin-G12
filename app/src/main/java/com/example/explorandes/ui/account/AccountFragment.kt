package com.example.explorandes.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.explorandes.MainActivity
import com.example.explorandes.R
import com.example.explorandes.models.User
import com.example.explorandes.utils.SessionManager

class AccountFragment : Fragment() {
    
    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userRole: TextView
    private lateinit var editProfileOption: LinearLayout
    private lateinit var languageOption: LinearLayout
    private lateinit var notificationsOption: LinearLayout
    private lateinit var signOutOption: LinearLayout
    
    // Mock user data
    private val currentUser = User(
        id = "1",
        firstName = "Luis Alfredo",
        lastName = "Castelblanco",
        email = "la.castelblanco@uniandes.edu.co",
        profileImageUrl = null,
        role = "Student"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        
        // Initialize views
        profileImage = view.findViewById(R.id.profileImage)
        userName = view.findViewById(R.id.userName)
        userRole = view.findViewById(R.id.userRole)
        editProfileOption = view.findViewById(R.id.editProfileOption)
        languageOption = view.findViewById(R.id.languageOption)
        notificationsOption = view.findViewById(R.id.notificationsOption)
        signOutOption = view.findViewById(R.id.signOutOption)
        
        setupUI()
        return view
    }
    
    private fun setupUI() {
        // Set user data
        userName.text = "${currentUser.firstName} ${currentUser.lastName}"
        userRole.text = currentUser.role
        
        // Set click listeners
        editProfileOption.setOnClickListener {
            navigateToEditProfile()
        }
        
        languageOption.setOnClickListener {
            navigateToLanguageSettings()
        }
        
        notificationsOption.setOnClickListener {
            navigateToNotificationSettings()
        }
        
        signOutOption.setOnClickListener {
            signOut()
        }
    }
    
    private fun navigateToEditProfile() {
        val fragment = EditProfileFragment()
        fragment.setUser(currentUser)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun navigateToLanguageSettings() {
        val fragment = LanguageFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun navigateToNotificationSettings() {
        val fragment = NotificationsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun signOut() {
        // Clear session
        val sessionManager = SessionManager(requireContext())
        sessionManager.clearAuthToken()
        
        // Navigate to login screen
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}