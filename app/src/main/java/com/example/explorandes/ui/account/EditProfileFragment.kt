package com.example.explorandes.ui.account

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.explorandes.R
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.google.android.material.snackbar.Snackbar
import com.example.explorandes.utils.ConnectivityHelper
import com.example.explorandes.utils.SessionManager

class EditProfileFragment : Fragment() {
    
    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var changeProfilePicture: TextView
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    
    private var user: User? = null
    private var selectedImageUri: Uri? = null
    
    // Permiso para acceder a la galería
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Resultado de seleccionar una imagen
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Cargar la imagen en la vista previa
                Glide.with(requireContext())
                    .load(uri)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }
    
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
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        
        setupUI()
        return view
    }
    
    private fun setupUI() {
        // Set user data if available
        user?.let {
            firstNameInput.setText(it.firstName ?: "")
            lastNameInput.setText(it.lastName ?: "")
            emailInput.setText(it.email)
            
            // Si el usuario tiene una URL de imagen, cargarla
            if (!it.profileImageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(it.profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(profileImage)
            }
            
            // Email is not editable
            emailInput.isEnabled = false
            
            // If we're offline, make all fields read-only
            if (!ConnectivityHelper(requireContext()).isInternetAvailable()) {
                firstNameInput.isEnabled = false
                lastNameInput.isEnabled = false
                changeProfilePicture.isEnabled = false
                saveButton.isEnabled = false
                
                // Show message
                Snackbar.make(
                    requireView(),
                    "Sin conexión. Los datos no pueden ser editados.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        
        // Set click listeners
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        changeProfilePicture.setOnClickListener {
            checkGalleryPermission()
        }
        
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        saveButton.setOnClickListener {
            saveUserData()
        }
    }
    
    private fun checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13 y superior usamos READ_MEDIA_IMAGES
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            // Para versiones anteriores usamos READ_EXTERNAL_STORAGE
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    
    private fun saveUserData() {
        // Validate inputs
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        
        if (firstName.isEmpty() && lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill at least one name field", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading indicator
        loadingProgressBar.visibility = View.VISIBLE
        saveButton.isEnabled = false
        
        // Get the current user and update with new data
        user?.let { currentUser ->
            val updatedUser = currentUser.copy(
                firstName = firstName,
                lastName = lastName
            )
            
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Si hay una imagen seleccionada, subirla primero
                    if (selectedImageUri != null) {
                        val updatedUserWithImage = uploadProfileImage(currentUser.id, selectedImageUri!!)
                        
                        if (updatedUserWithImage != null) {
                            // Si la imagen se subió correctamente, continuar con la actualización del perfil
                            // con los datos actualizados del usuario incluyendo la nueva imagen
                            val finalUser = updatedUserWithImage.copy(
                                firstName = firstName,
                                lastName = lastName
                            )
                            
                            val profileResponse = ApiClient.apiService.updateUser(currentUser.id, finalUser)
                            
                            if (profileResponse.isSuccessful) {
                                handleSuccessfulUpdate()
                            } else {
                                handleUpdateError("Failed to update profile: ${profileResponse.code()}")
                            }
                        } else {
                            // Error al subir la imagen
                            handleUpdateError("Failed to upload profile image")
                        }
                    } else {
                        // No hay imagen para subir, solo actualizar datos de perfil
                        val response = ApiClient.apiService.updateUser(currentUser.id, updatedUser)
                        
                        if (response.isSuccessful && response.body() != null) {
                            handleSuccessfulUpdate()
                        } else {
                            handleUpdateError("Failed to update profile: ${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    handleException(e)
                } finally {
                    loadingProgressBar.visibility = View.GONE
                }
            }
        } ?: run {
            // No user data available
            Toast.makeText(requireContext(), "No user data available to update", Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            loadingProgressBar.visibility = View.GONE
        }
    }
    
    private suspend fun uploadProfileImage(userId: Long, imageUri: Uri): User? {
        return try {
            val context = requireContext()
            val contentResolver = context.contentResolver
            
            // Crear archivo temporal
            val file = File(context.cacheDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            
            // Copiar contenido de Uri al archivo
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
            
            // Crear MultipartBody.Part
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
            
            // Subir imagen
            val response = ApiClient.apiService.uploadProfileImage(userId, imagePart)
            
            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun handleSuccessfulUpdate() {
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }
    
    private fun handleUpdateError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        saveButton.isEnabled = true
    }
    
    private fun handleException(e: Exception) {
        when (e) {
            is HttpException -> {
                Toast.makeText(
                    requireContext(),
                    "Server error: ${e.code()} - ${e.message()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is IOException -> {
                Toast.makeText(
                    requireContext(),
                    "Network error: Check your connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        saveButton.isEnabled = true
    }
}