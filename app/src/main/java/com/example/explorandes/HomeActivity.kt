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
import com.example.explorandes.adapters.EventAdapter
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.Building
import com.example.explorandes.models.Event
import com.example.explorandes.ui.account.AccountFragment
import com.example.explorandes.ui.navigation.NavigationFragment
import com.example.explorandes.utils.SessionManager
import com.example.explorandes.viewmodels.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var buildingsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var fragmentContainer: View
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: HomeViewModel
    private lateinit var buildingAdapter: BuildingAdapter
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ApiClient.init(applicationContext)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            Log.d("HomeActivity", "No active session, redirecting to login")
            navigateToLogin()
            return
        }

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        setupViewModelObservers()
        viewModel.loadUserData(sessionManager)
        initializeUI()
    }

    private fun setupViewModelObservers() {
        viewModel.user.observe(this) { user ->
            Log.d("HomeActivity", "User loaded: ${user.username}")
            sessionManager.saveUserInfo(user.id, user.email, user.username)
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Log.e("HomeActivity", "Error: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()

                if (it.contains("401") || it.contains("no encontró")) {
                    sessionManager.logout()
                    navigateToLogin()
                }
            }
        }

        viewModel.isLoading.observe(this) { /* loading UI optional */ }

        viewModel.buildings.observe(this) { buildings ->
            if (buildings.isNotEmpty()) {
                buildingAdapter.updateData(buildings)
            } else {
                Log.d("HomeActivity", "No buildings data received")
            }
        }

        viewModel.events.observe(this) { events ->
            if (events != null && events.isNotEmpty()) {
                eventAdapter.submitList(events)
            } else {
                Log.d("HomeActivity", "No events received")
            }
        }

    }

    private fun initializeUI() {
        nestedScrollView = findViewById(R.id.nestedScrollView)
        fragmentContainer = findViewById(R.id.fragment_container)

        val userName = sessionManager.getUsername() ?: "Usuario"
        findViewById<TextView>(R.id.greeting_text).text = "Hola, $userName"

        setupCategoryIcons()
        setupRecyclerViews()
        setupBottomNavigation()
        setupClickListeners()

        viewModel.loadBuildings()
        viewModel.loadEvents()

        if (intent.getBooleanExtra("OPEN_NAVIGATION", false)) {
            findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId =
                R.id.navigation_navigate
        }
    }

    private fun setupCategoryIcons() {
        val categoryBuildings = findViewById<View>(R.id.category_buildings)
        val categoryFood = findViewById<View>(R.id.category_food)
        val categoryServices = findViewById<View>(R.id.category_services)

        findViewById<View>(R.id.category_events).visibility = View.GONE
        findViewById<View>(R.id.category_study).visibility = View.GONE

        categoryBuildings.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_building)
        categoryBuildings.findViewById<TextView>(R.id.category_name).text = getString(R.string.buildings)

        categoryFood.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_food)
        categoryFood.findViewById<TextView>(R.id.category_name).text = getString(R.string.food_rest)

        categoryServices.findViewById<ImageView>(R.id.category_icon).setImageResource(R.drawable.ic_services)
        categoryServices.findViewById<TextView>(R.id.category_name).text = getString(R.string.services)

        categoryBuildings.setOnClickListener { viewModel.loadBuildingsByCategory("Buildings") }
        categoryFood.setOnClickListener { viewModel.loadBuildingsByCategory("Food") }
        categoryServices.setOnClickListener { viewModel.loadBuildingsByCategory("Services") }
    }

    private fun setupRecyclerViews() {
        buildingsRecyclerView = findViewById(R.id.buildings_recycler)
        buildingsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        buildingAdapter = BuildingAdapter(emptyList()) { building ->
            val intent = Intent(this, BuildingDetailActivity::class.java).apply {
                putExtra("BUILDING_ID", building.id)
            }
            startActivity(intent)
        }
        buildingsRecyclerView.adapter = buildingAdapter

        eventsRecyclerView = findViewById(R.id.events_recycler)
        eventAdapter = EventAdapter { event ->
            Toast.makeText(this, "Evento: ${event.title}", Toast.LENGTH_SHORT).show()
        }
        eventsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventsRecyclerView.adapter = eventAdapter
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showHomeContent(); true
                }
                R.id.navigation_favorites -> {
                    Toast.makeText(this, getString(R.string.favorites), Toast.LENGTH_SHORT).show(); true
                }
                R.id.navigation_navigate -> {
                    loadFragment(NavigationFragment()); true
                }
                R.id.navigation_notifications -> {
                    Toast.makeText(this, getString(R.string.notifications), Toast.LENGTH_SHORT).show(); true
                }
                R.id.navigation_account -> {
                    loadFragment(AccountFragment()); true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<TextView>(R.id.see_all_buildings).setOnClickListener {
            findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId =
                R.id.navigation_navigate
        }

        findViewById<TextView>(R.id.see_all_events).setOnClickListener {
            Toast.makeText(this, "Ver todos los eventos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHomeContent() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        nestedScrollView.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
    }

    private fun loadFragment(fragment: Fragment) {
        nestedScrollView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
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
