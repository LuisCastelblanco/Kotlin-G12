package com.example.explorandes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorandes.adapters.BuildingAdapter
import com.example.explorandes.adapters.RecommendationAdapter
import com.example.explorandes.models.Building
import com.example.explorandes.models.Recommendation
import com.example.explorandes.models.RecommendationType
import com.example.explorandes.ui.account.AccountFragment
import com.example.explorandes.ui.navigation.NavigationFragment
import com.example.explorandes.utils.SessionManager
import com.example.explorandes.viewmodels.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var buildingsRecyclerView: RecyclerView
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var fragmentContainer: View
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Verificar si hay un usuario autenticado
        if (!sessionManager.isLoggedIn()) {
            Log.d("HomeActivity", "No hay sesión activa, redirigiendo a login")
            navigateToLogin()
            return
        }

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Configurar observadores para el ViewModel
        setupViewModelObservers()

        // Cargar datos del usuario
        viewModel.loadUserData(sessionManager)

        // Inicializar UI
        initializeUI()
    }

    private fun setupViewModelObservers() {
        // Observar cambios en los datos del usuario
        viewModel.user.observe(this) { user ->
            Log.d("HomeActivity", "Usuario cargado: ${user.username}")
            // Actualizar la información del usuario en SessionManager
            sessionManager.saveUserInfo(user.id, user.email, user.username)
        }

        // Observar errores
        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Log.e("HomeActivity", "Error: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()

                // Si hay un error de autenticación, redirigir a login
                if (it.contains("401") || it.contains("no encontró")) {
                    sessionManager.logout()
                    navigateToLogin()
                }
            }
        }

        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            // Aquí podrías mostrar/ocultar un indicador de carga si lo necesitas
        }
    }

    private fun initializeUI() {
        // Find views needed for navigation
        nestedScrollView = findViewById(R.id.nestedScrollView)
        fragmentContainer = findViewById(R.id.fragment_container)

        // Setup UI components
        setupCategoryIcons()
        setupRecyclerViews()
        setupBottomNavigation()
        setupClickListeners()

        // Check if we should open directly to the navigation tab
        if (intent.getBooleanExtra("OPEN_NAVIGATION", false)) {
            val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigation.selectedItemId = R.id.navigation_navigate
        }
    }

    private fun setupCategoryIcons() {
        // Find all category views
        val categoryBuildings = findViewById<View>(R.id.category_buildings)
        val categoryEvents = findViewById<View>(R.id.category_events)
        val categoryFood = findViewById<View>(R.id.category_food)
        val categoryStudy = findViewById<View>(R.id.category_study)
        val categoryServices = findViewById<View>(R.id.category_services)

        // Setup Buildings category
        categoryBuildings.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_building)
        categoryBuildings.findViewById<TextView>(R.id.category_name).text = getString(R.string.buildings)

        // Setup Events category
        categoryEvents.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_event)
        categoryEvents.findViewById<TextView>(R.id.category_name).text = getString(R.string.events)

        // Setup Food & Rest category
        categoryFood.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_food)
        categoryFood.findViewById<TextView>(R.id.category_name).text = getString(R.string.food_rest)

        // Setup Study Spaces category
        categoryStudy.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_study)
        categoryStudy.findViewById<TextView>(R.id.category_name).text = getString(R.string.study_spaces)

        // Setup Services category
        categoryServices.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_services)
        categoryServices.findViewById<TextView>(R.id.category_name).text = getString(R.string.services)

        // Set click listeners for categories
        categoryBuildings.setOnClickListener {
            Toast.makeText(this, getString(R.string.buildings), Toast.LENGTH_SHORT).show()
        }
        categoryEvents.setOnClickListener {
            Toast.makeText(this, getString(R.string.events), Toast.LENGTH_SHORT).show()
        }
        categoryFood.setOnClickListener {
            Toast.makeText(this, getString(R.string.food_rest), Toast.LENGTH_SHORT).show()
        }
        categoryStudy.setOnClickListener {
            Toast.makeText(this, getString(R.string.study_spaces), Toast.LENGTH_SHORT).show()
        }
        categoryServices.setOnClickListener {
            Toast.makeText(this, getString(R.string.services), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerViews() {
        // Setup Buildings RecyclerView
        buildingsRecyclerView = findViewById(R.id.buildings_recycler)
        buildingsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Create sample data for buildings
        val buildings = listOf(
            Building("1", "Bloque ML", "Faculty of Engineering", R.drawable.profile_placeholder),
            Building("2", "Bloque W", "Faculty of Rights", R.drawable.profile_placeholder),
            Building("3", "Bloque C", "Student Center", R.drawable.profile_placeholder)
        )

        val buildingAdapter = BuildingAdapter(buildings) { building ->
            Toast.makeText(this, "Selected: ${building.name}", Toast.LENGTH_SHORT).show()
        }
        buildingsRecyclerView.adapter = buildingAdapter

        // Setup Recommendations RecyclerView
        recommendationsRecyclerView = findViewById(R.id.recommendations_recycler)
        recommendationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Create sample data for recommendations
        val recommendations = listOf(
            Recommendation("1", "Medium", "Anestesia Alberto López", R.drawable.profile_placeholder, RecommendationType.PODCAST),
            Recommendation("2", "CHOBA", "Audición Alberto López", R.drawable.profile_placeholder, RecommendationType.DOCUMENTARY),
            Recommendation("3", "C4", "Audición María Pérez", R.drawable.profile_placeholder, RecommendationType.THEATER)
        )

        val recommendationAdapter = RecommendationAdapter(recommendations) { recommendation ->
            Toast.makeText(this, "Selected: ${recommendation.title}", Toast.LENGTH_SHORT).show()
        }
        recommendationsRecyclerView.adapter = recommendationAdapter
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showHomeContent()
                    true
                }
                R.id.navigation_favorites -> {
                    Toast.makeText(this, getString(R.string.favorites), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_navigate -> {
                    loadFragment(NavigationFragment())
                    true
                }
                R.id.navigation_notifications -> {
                    Toast.makeText(this, getString(R.string.notifications), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_account -> {
                    loadFragment(AccountFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        // Set click listeners for the "See all" buttons
        findViewById<TextView>(R.id.see_all_buildings).setOnClickListener {
            Toast.makeText(this, "See all buildings", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.see_all_recommendations).setOnClickListener {
            Toast.makeText(this, "See all recommendations", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHomeContent() {
        // Hide any fragments and show the main content
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        // Show the home content
        nestedScrollView.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
    }

    private fun loadFragment(fragment: Fragment) {
        // Hide the home content
        nestedScrollView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        // Load the fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}