package com.example.geosphere.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geosphere.adapters.LeaderboardAdapter
import com.example.geosphere.databinding.ActivityAdminResultsBinding
import com.example.geosphere.utils.FirebaseHelper
import kotlinx.coroutines.launch

class AdminResultsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminResultsBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper(this)

        setupToolbar()
        setupRecyclerView()
        loadResults()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter(type = "all") // Show total points
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@AdminResultsActivity)
            adapter = leaderboardAdapter
            addItemDecoration(
                DividerItemDecoration(this@AdminResultsActivity, DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun loadResults() {
        lifecycleScope.launch {
            val result = firebaseHelper.getLeaderboardByType("all")
            
            if (result.isSuccess) {
                val entries = result.getOrDefault(emptyList())
                if (entries.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvResults.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvResults.visibility = View.VISIBLE
                    leaderboardAdapter.submitList(entries)
                }
            } else {
                binding.tvEmpty.text = "Failed to load players."
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
