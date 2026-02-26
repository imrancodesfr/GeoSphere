package com.example.geosphere.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geosphere.R
import com.example.geosphere.adapters.LeaderboardAdapter
import com.example.geosphere.databinding.ActivityLeaderboardBinding
import com.example.geosphere.utils.FirebaseHelper
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    // Map tab position → filter type
    private val tabTypes = listOf("daily", "weekly", "all")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()

        setupToolbar()
        setupRecyclerView()
        setupTabs()

        // Load daily leaderboard by default (tab 0)
        loadLeaderboard("daily")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.leaderboard)
        }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
            addItemDecoration(
                DividerItemDecoration(this@LeaderboardActivity, DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val type = tabTypes.getOrElse(tab?.position ?: 0) { "daily" }
                loadLeaderboard(type)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadLeaderboard(type: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility     = View.GONE
        leaderboardAdapter.type = type
        leaderboardAdapter.submitList(emptyList())

        lifecycleScope.launch {
            val result = firebaseHelper.getLeaderboardByType(type)
            binding.progressBar.visibility = View.GONE

            if (result.isSuccess) {
                val entries = result.getOrDefault(emptyList())
                if (entries.isEmpty()) {
                    val emptyMsg = when (type) {
                        "daily"  -> "No one has played today yet. Be the first! 🏆"
                        "weekly" -> "No activity this week. Start playing! 🌍"
                        else     -> "No players yet. Be the first! 🌏"
                    }
                    binding.tvEmpty.text = emptyMsg
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    leaderboardAdapter.submitList(entries)
                }
            } else {
                binding.tvEmpty.text = "Failed to load. Check your connection."
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}