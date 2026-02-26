package com.example.geosphere.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geosphere.databinding.ItemLeaderboardBinding
import com.example.geosphere.models.LeaderboardEntry

class LeaderboardAdapter(
    var type: String = "all"   // "all" | "daily" | "weekly"
) : ListAdapter<LeaderboardEntry, LeaderboardAdapter.LeaderboardViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LeaderboardViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            // Show correct score for the current tab type
            val displayScore = when (type) {
                "daily"  -> entry.dailyPoints
                "weekly" -> entry.weeklyPoints
                else     -> entry.totalPoints
            }

            // Rank with medal emojis for top 3
            binding.tvRank.text = when (entry.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#${entry.rank}"
            }

            binding.tvUsername.text  = entry.username
            binding.tvQuizzes.text   = "${entry.quizzesPlayed} quizzes played"
            binding.tvPoints.text    = "$displayScore pts"
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry) =
            oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry) =
            oldItem == newItem
    }
}
