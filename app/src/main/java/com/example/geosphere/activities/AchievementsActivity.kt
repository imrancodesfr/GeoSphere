package com.example.geosphere.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geosphere.R
import com.example.geosphere.adapters.AchievementsAdapter
import com.example.geosphere.databinding.ActivityAchievementsBinding
import com.example.geosphere.utils.AchievementMilestones
import com.example.geosphere.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AchievementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var achievementsAdapter: AchievementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper()

        setupToolbar()
        setupRecyclerView()
        loadAchievements()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.achievements)
        }
    }

    private fun setupRecyclerView() {
        achievementsAdapter = AchievementsAdapter()
        binding.rvAchievements.apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            adapter = achievementsAdapter
        }
    }

    private fun loadAchievements() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Not logged in — show all milestones as locked
            achievementsAdapter.totalCorrect = 0
            achievementsAdapter.submitList(AchievementMilestones.ALL)
            updateProgressCard(0)
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = firebaseHelper.getAllMilestonesWithStatus(userId)
            binding.progressBar.visibility = View.GONE

            if (result.isSuccess) {
                val (milestones, totalCorrect) = result.getOrDefault(
                    Pair(AchievementMilestones.ALL, 0)
                )
                achievementsAdapter.totalCorrect = totalCorrect
                achievementsAdapter.submitList(milestones)
                binding.cardProgress.visibility = View.VISIBLE
                updateProgressCard(totalCorrect)
            } else {
                // Still show all milestones (all locked) on error
                achievementsAdapter.totalCorrect = 0
                achievementsAdapter.submitList(AchievementMilestones.ALL)
                updateProgressCard(0)
            }
        }
    }

    private fun updateProgressCard(totalCorrect: Int) {
        binding.tvCorrectCount.text = "$totalCorrect correct answers total"

        val next = AchievementMilestones.getNext(totalCorrect)
        binding.tvNextMilestone.text = if (next != null) {
            val remaining = next.requiredCorrect - totalCorrect
            "Next: ${next.emoji} ${next.name} — ${remaining} more to go!"
        } else {
            "🎉 All achievements unlocked! You are a Globe Trotter Legend!"
        }
        binding.cardProgress.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}