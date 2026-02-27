package com.example.geosphere.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geosphere.adapters.ReportedQuestionsAdapter
import com.example.geosphere.databinding.ActivityAdminReportsBinding
import com.example.geosphere.utils.FirebaseHelper
import kotlinx.coroutines.launch

class AdminReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminReportsBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var reportsAdapter: ReportedQuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper(this)

        setupToolbar()
        setupRecyclerView()
        loadReports()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        reportsAdapter = ReportedQuestionsAdapter(
            onDismissClicked = { report ->
                lifecycleScope.launch {
                    val result = firebaseHelper.deleteReport(report.id)
                    if (result.isSuccess) {
                        Toast.makeText(this@AdminReportsActivity, "Report dismissed", Toast.LENGTH_SHORT).show()
                        loadReports() // Refresh list
                    } else {
                        Toast.makeText(this@AdminReportsActivity, "Failed to dismiss", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onEditClicked = { report ->
                // In a real app we might navigate to an edit screen
                // For now, let's just show a toast
                Toast.makeText(this, "Edit functionality coming soon!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(this@AdminReportsActivity)
            adapter = reportsAdapter
        }
    }

    private fun loadReports() {
        lifecycleScope.launch {
            val result = firebaseHelper.getReportedQuestions()
            
            if (result.isSuccess) {
                val entries = result.getOrDefault(emptyList())
                if (entries.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvReports.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvReports.visibility = View.VISIBLE
                    reportsAdapter.submitList(entries)
                }
            } else {
                binding.tvEmpty.text = "Failed to load reports."
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
