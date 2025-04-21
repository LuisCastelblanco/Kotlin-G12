package com.example.explorandes.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.explorandes.R

class LanguageFragment : Fragment() {
    
    private lateinit var backButton: ImageView
    private lateinit var languageGroup: RadioGroup
    private lateinit var cancelButton: Button
    private lateinit var applyButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_language, container, false)
        
        // Initialize views
        backButton = view.findViewById(R.id.backButton)
        languageGroup = view.findViewById(R.id.languageGroup)
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
            applyLanguageSettings()
        }
    }
    
    private fun applyLanguageSettings() {
        // Get selected language
        val selectedLanguage = when (languageGroup.checkedRadioButtonId) {
            R.id.englishUS -> "English (US)"
            R.id.englishUK -> "English (UK)"
            R.id.spanish -> "Spanish"
            R.id.portuguese -> "Portuguese"
            else -> "English (US)"
        }
        
        // In a real app, this would update the app's language settings
        Toast.makeText(requireContext(), "Language changed to $selectedLanguage", Toast.LENGTH_SHORT).show()
        
        // Return to account screen
        parentFragmentManager.popBackStack()
    }
}