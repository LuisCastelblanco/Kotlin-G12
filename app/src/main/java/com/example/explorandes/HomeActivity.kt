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
import com.example.explorandes.adapters.RecommendationAdapter
import com.example.explorandes.api.ApiClient
import com.example.explorandes.models.Recommendation
import com.example.explorandes.models.RecommendationType
import com.example.explorandes.ui.buildings.BuildingsListFragment
import com.example.explorandes.utils.SessionManager
import com.example.explorandes.viewmodels.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : BaseActivity() {

    private lateinit var buildingsRecyclerView: RecyclerView
    private lateinit var recommendationsRecyclerView: RecyclerView
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
            navigateToLogin()
            return
        }

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupViewModelObservers()
        viewModel.loadUserData(sessionManager)
        initializeUI()

    }

    private fun setupViewModelObservers() {
        viewModel.user.observe(this) { user ->
            sessionManager.saveUserInfo(user.id, user.email, user.username)
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it.contains("401") || it.contains("no encontró")) {
                    sessionManager.logout()
                    navigateToLogin()
                }
            }
        }

        viewModel.isLoading.observe(this) { /* opcional */ }

        viewModel.buildings.observe(this) { buildings ->
            buildingAdapter.updateData(buildings)
        }

        viewModel.events.observe(this) { events ->
            eventAdapter.submitList(events)
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
            findViewById<BottomNavigationView>(R.id.bottom_navigation)
                .selectedItemId = R.id.navigation_navigate
        }
    }

    private fun setupCategoryIcons() {
        val categoryAll = findViewById<View>(R.id.category_all)
        val categoryBuildings = findViewById<View>(R.id.category_buildings)
        val categoryFood = findViewById<View>(R.id.category_food)
        val categoryServices = findViewById<View>(R.id.category_services)

        findViewById<View>(R.id.category_events).visibility = View.GONE
        findViewById<View>(R.id.category_study).visibility = View.GONE

        // Set up All category
        categoryAll.findViewById<ImageView>(R.id.category_icon)
            .setImageResource(R.drawable.ic_all)
        categoryAll.findViewById<TextView>(R.id.category_name)
            .text = getString(R.string.all)

        categoryBuildings.findViewById<ImageView>(R.id.category_icon)
            .setImageResource(R.drawable.ic_building)
        categoryBuildings.findViewById<TextView>(R.id.category_name)
            .text = getString(R.string.buildings)

        categoryFood.findViewById<ImageView>(R.id.category_icon)
            .setImageResource(R.drawable.ic_food)
        categoryFood.findViewById<TextView>(R.id.category_name)
            .text = getString(R.string.food_rest)

        categoryServices.findViewById<ImageView>(R.id.category_icon)
            .setImageResource(R.drawable.ic_services)
        categoryServices.findViewById<TextView>(R.id.category_name)
            .text = getString(R.string.services)

        // Add click listener for All category
        categoryAll.setOnClickListener {
            viewModel.loadBuildings() // This loads all buildings without category filtering
        }

        categoryBuildings.setOnClickListener {
            viewModel.loadBuildingsByCategory("Buildings")
        }

        categoryFood.setOnClickListener {
            viewModel.loadBuildingsByCategory("Food")
        }

        categoryServices.setOnClickListener {
            viewModel.loadBuildingsByCategory("Services")
        }
    }

    private fun setupRecyclerViews() {
        // Buildings
        buildingsRecyclerView = findViewById(R.id.buildings_recycler)
        buildingsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        buildingAdapter = BuildingAdapter(
            emptyList(),
            onBuildingClicked = { building ->
                val intent = Intent(this, BuildingDetailActivity::class.java).apply {
                    putExtra("BUILDING_ID", building.id)
                }
                startActivity(intent)
            }
        )
        buildingsRecyclerView.adapter = buildingAdapter

        // Events
        eventsRecyclerView = findViewById(R.id.events_recycler)
        eventsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventAdapter = EventAdapter { event ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("EVENT", event)
            startActivity(intent)
        }
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
                    startActivity(Intent(this, MapActivity::class.java)); true
                }
                R.id.navigation_view -> {
                    Toast.makeText(this, getString(R.string.view), Toast.LENGTH_SHORT).show(); true
                }
                R.id.navigation_account -> {
                    loadFragment(com.example.explorandes.ui.account.AccountFragment()); true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<TextView>(R.id.see_all_buildings).setOnClickListener {
            // Show buildings list fragment
            nestedScrollView.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BuildingsListFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        findViewById<TextView>(R.id.see_all_events).setOnClickListener {
            // Show events list fragment using your existing implementation
            nestedScrollView.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE

            // Create and use the existing EventListFragment
            val eventListFragment = com.example.explorandes.fragments.EventListFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, eventListFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showHomeContent() {
        supportFragmentManager.findFragmentById(R.id.fragment_container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        nestedScrollView.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
    }

    private fun loadFragment(fragment: Fragment) {
        nestedScrollView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
