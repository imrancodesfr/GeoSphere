package com.example.geosphere.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.geosphere.R
import com.example.geosphere.databinding.ActivityResultBinding

class ResultActivity : BaseActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_result)

        val score        = intent.getIntExtra("score", 0)
        val total        = intent.getIntExtra("total", 0)
        val category     = intent.getStringExtra("category") ?: ""
        val categoryId   = intent.getStringExtra("category_id") ?: ""
        val achievements = intent.getStringArrayListExtra("achievements") ?: arrayListOf()

        displayResults(score, total, category, achievements)
        setupButtons(score, total, category, categoryId)
    }

    private fun displayResults(score: Int, total: Int, category: String, achievements: ArrayList<String>) {
        binding.tvCategory.text = category
        binding.tvScore.text    = getString(R.string.score_format, score, total)

        val percentage = if (total > 0) (score * 100) / total else 0
        binding.tvPercentage.text = getString(R.string.percentage_format, percentage)

        binding.progressCircle.progress = percentage.toFloat()

        binding.tvMessage.text = when {
            percentage == 100 -> getString(R.string.perfect)
            percentage >= 80  -> getString(R.string.excellent)
            percentage >= 60  -> getString(R.string.good_effort)
            percentage >= 40  -> getString(R.string.not_bad)
            else              -> getString(R.string.keep_exploring)
        }

        if (achievements.isNotEmpty()) {
            binding.layoutAchievements.visibility = View.VISIBLE
            binding.tvAchievements.text = achievements.joinToString("\n") { "🏆 $it" }
        } else {
            binding.layoutAchievements.visibility = View.GONE
        }
    }

    private fun setupButtons(score: Int, total: Int, category: String, categoryId: String) {
        // Play Again — restart the same category
        binding.btnPlayAgain.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java).apply {
                putExtra("category_id", categoryId)
                putExtra("category_name", category)
            }
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        // Go to Leaderboard
        binding.btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Share result text
        binding.btnShare.setOnClickListener {
            val shareText = getString(R.string.share_text, score, total, category)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }
}