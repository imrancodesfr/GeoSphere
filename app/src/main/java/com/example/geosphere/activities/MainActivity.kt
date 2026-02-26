package com.example.geosphere.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.geosphere.R
import com.example.geosphere.adapters.CategoryAdapter
import com.example.geosphere.databinding.ActivityMainBinding
import com.example.geosphere.models.Category
import com.example.geosphere.theme.ColorPalette
import com.example.geosphere.utils.FirebaseHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper()

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        loadCategories()
        setupClickListeners()
        startAnimations()
        loadUserData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this)

        val headerView = binding.navigationView.getHeaderView(0)
        val currentUser = auth.currentUser

        headerView.findViewById<TextView>(R.id.tvUserName).text =
            currentUser?.displayName ?: getString(R.string.default_username)
        headerView.findViewById<TextView>(R.id.tvUserEmail).text =
            currentUser?.email ?: getString(R.string.default_email)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            val intent = Intent(this, QuizActivity::class.java).apply {
                putExtra("category_id", category.id)
                putExtra("category_name", category.name)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val sampleCategories = listOf(
                    Category("asia", getString(R.string.asia), "🌏",
                        getString(R.string.asia_desc), 120, ColorPalette.BoldBlue1),
                    Category("europe", getString(R.string.europe), "🌍",
                        getString(R.string.europe_desc), 150, ColorPalette.Turquoise),
                    Category("americas", getString(R.string.americas), "🌎",
                        getString(R.string.americas_desc), 130, ColorPalette.BoldOrange),
                    Category("africa", getString(R.string.africa), "🌍",
                        getString(R.string.africa_desc), 100, ColorPalette.CalmGreen),
                    Category("flags", getString(R.string.flags), "🏁",
                        getString(R.string.flags_desc), 200, ColorPalette.StrikingRed),
                    Category("capitals", getString(R.string.capitals), "🏛️",
                        getString(R.string.capitals_desc), 180, ColorPalette.AccentPurple),
                    Category("landmarks", getString(R.string.landmarks), "🗽",
                        getString(R.string.landmarks_desc), 90, ColorPalette.DeepTeal),
                    Category("world", getString(R.string.world), "🌐",
                        getString(R.string.world_desc), 300, ColorPalette.BoldBlue3)
                )

                categories.clear()
                categories.addAll(sampleCategories)
                categoryAdapter.submitList(categories.toList())

                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                e.printStackTrace()
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            // TODO: Fetch user data from Firebase
        }
    }

    private fun setupClickListeners() {
        binding.fabPlay.setOnClickListener {
            val randomCategory = categories.randomOrNull()
            if (randomCategory != null) {
                val intent = Intent(this, QuizActivity::class.java).apply {
                    putExtra("category_id", randomCategory.id)
                    putExtra("category_name", randomCategory.name)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun startAnimations() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.fabPlay.startAnimation(slideUp)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvCategories.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on home
            }
            R.id.nav_leaderboard -> {
                startActivity(Intent(this, LeaderboardActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_achievements -> {
                startActivity(Intent(this, AchievementsActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_logout -> {
                logout()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}