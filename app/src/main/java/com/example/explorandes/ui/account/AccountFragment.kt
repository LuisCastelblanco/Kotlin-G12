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
import androidx.lifecycle.lifecycleScope
import com.example.explorandes.MainActivity
import com.example.explorandes.R
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.User
import com.example.explorandes.utils.SessionManager
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.example.explorandes.utils.ConnectivityHelper
import android.util.Log

class AccountFragment : Fragment() {
    
    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userRole: TextView
    private lateinit var editProfileOption: LinearLayout
    private lateinit var languageOption: LinearLayout
    private lateinit var notificationsOption: LinearLayout
    private lateinit var signOutOption: LinearLayout
    private lateinit var loadingView: View
    
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null
    private var rootView: View? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        rootView = view
        
        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())
        
        // Initialize views
        profileImage = view.findViewById(R.id.profileImage)
        userName = view.findViewById(R.id.userName)
        userRole = view.findViewById(R.id.userRole)
        editProfileOption = view.findViewById(R.id.editProfileOption)
        languageOption = view.findViewById(R.id.languageOption)
        notificationsOption = view.findViewById(R.id.notificationsOption)
        signOutOption = view.findViewById(R.id.signOutOption)
        loadingView = view.findViewById(R.id.loadingView)
        
        // Set up click listeners
        setupClickListeners()
        
        // Load user data after views are initialized
        loadUserData()
        
        return view
    }
    
    private fun loadUserData() {
        showLoading(true)
        
        // First, try to load from cache (SessionManager)
        val userId = sessionManager.getUserId()
        
        if (userId <= 0) {
            // No cached user data, redirect to login
            Toast.makeText(requireContext(), "No se encontró información de usuario", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }
        
        // Show cached data immediately
        val cachedUserName = sessionManager.getCachedUserName()
        val cachedEmail = sessionManager.getEmail() ?: ""
        
        // Update UI with cached data
        userName.text = cachedUserName
        userRole.text = "Estudiante" // Default value
        
        // Load cached profile picture if available
        val cachedProfilePicUrl = sessionManager.getCachedProfilePicUrl()
        if (!cachedProfilePicUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(cachedProfilePicUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .circleCrop()
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.profile_placeholder)
        }
        
        // Hide loading once cached data is shown
        showLoading(false)
        
        // Then try to get fresh data from API if online
        if (ConnectivityHelper(requireContext()).isInternetAvailable()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.getUserById(userId)
                    
                    if (response.isSuccessful && response.body() != null) {
                        currentUser = response.body()
                        updateUI()
                    } else {
                        // API error but we already have cached data shown
                        if (response.code() == 401) {
                            // Token invalid or expired
                            sessionManager.logout()
                            navigateToLogin()
                        }
                    }
                } catch (e: Exception) {
                    // Network error, but we already have cached data shown
                    Log.e("AccountFragment", "Error de conexión: ${e.message}")
                }
            }
        } else {
            // Offline - we've already shown cached data
            if (isAdded && view != null) {  // Check if fragment is still attached
                Snackbar.make(
                    requireView(),
                    "Sin conexión. Mostrando datos almacenados localmente.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun updateUI() {
        if (!isAdded || view == null) return  // Check if fragment is still attached
        
        currentUser?.let { user ->
            // Construir nombre completo
            val displayName = if (!user.firstName.isNullOrEmpty() || !user.lastName.isNullOrEmpty()) {
                val firstName = user.firstName ?: ""
                val lastName = user.lastName ?: ""
                "$firstName $lastName".trim()
            } else {
                user.username
            }
            
            userName.text = displayName
            userRole.text = "Estudiante" // Valor por defecto
            
            // Cargar imagen de perfil si existe
            if (!user.profileImageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(profileImage)
            } else {
                // Si no hay imagen de perfil, usar imagen predeterminada
                profileImage.setImageResource(R.drawable.profile_placeholder)
            }
        }
    }
    
    private fun setupClickListeners() {
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
        currentUser?.let { user ->
            val fragment = EditProfileFragment()
            fragment.setUser(user)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        } ?: Toast.makeText(requireContext(), "Información de usuario no disponible", Toast.LENGTH_SHORT).show()
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
        sessionManager.logout()
        
        // Navigate to login screen
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (::loadingView.isInitialized) {
            loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}