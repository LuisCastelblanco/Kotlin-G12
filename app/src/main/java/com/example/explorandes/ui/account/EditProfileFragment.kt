package com.example.explorandes.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.explorandes.R
import com.example.explorandes.models.User

class EditProfileFragment : Fragment() {
    
    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var changeProfilePicture: TextView
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    
    private var user: User? = null
    
    fun setUser(user: User) {
        this.user = user
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        
        // Initialize views
        backButton = view.findViewById(R.id.backButton)
        profileImage = view.findViewById(R.id.profileImage)
        changeProfilePicture = view.findViewById(R.id.changeProfilePicture)
        firstNameInput = view.findViewById(R.id.firstNameInput)
        lastNameInput = view.findViewById(R.id.lastNameInput)
        emailInput = view.findViewById(R.id.emailInput)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)
        
        setupUI()
        return view
    }
    
    private fun setupUI() {
        // Set user data if available
        user?.let {
            firstNameInput.setText(it.firstName)
            lastNameInput.setText(it.lastName)
            emailInput.setText(it.email)
        }
        
        // Set click listeners
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        changeProfilePicture.setOnClickListener {
            // TODO: Implement profile picture change functionality
            Toast.makeText(requireContext(), "Change profile picture", Toast.LENGTH_SHORT).show()
        }
        
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        saveButton.setOnClickListener {
            saveUserData()
        }
    }
    
    private fun saveUserData() {
        // Validate inputs
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Update user data
        // In a real app, this would make an API call to update the user's profile
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        
        // Return to account screen
        parentFragmentManager.popBackStack()
    }
}