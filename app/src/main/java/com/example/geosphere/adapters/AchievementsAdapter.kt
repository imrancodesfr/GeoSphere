package com.example.geosphere.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geosphere.databinding.ItemAchievementBinding
import com.example.geosphere.utils.AchievementMilestone

class AchievementsAdapter :
    ListAdapter<AchievementMilestone, AchievementsAdapter.AchievementViewHolder>(DiffCallback()) {

    /** Set this to the user's total correct answers before submitting the list */
    var totalCorrect: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AchievementViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(milestone: AchievementMilestone) {
            val isEarned = totalCorrect >= milestone.requiredCorrect

            binding.tvEmoji.text       = if (isEarned) milestone.emoji else "🔒"
            binding.tvName.text        = milestone.name
            binding.tvDescription.text = milestone.description

            if (isEarned) {
                binding.tvRequired.text = "✅ Unlocked"
                binding.tvRequired.setTextColor(Color.parseColor("#27AE60"))
                // Full opacity for earned
                binding.root.alpha = 1f
            } else {
                val remaining = milestone.requiredCorrect - totalCorrect
                binding.tvRequired.text = "🔒 Need $remaining more correct answers"
                binding.tvRequired.setTextColor(Color.parseColor("#BDC3C7"))
                // Dimmed for locked
                binding.root.alpha = 0.55f
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AchievementMilestone>() {
        override fun areItemsTheSame(oldItem: AchievementMilestone, newItem: AchievementMilestone) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AchievementMilestone, newItem: AchievementMilestone) =
            oldItem == newItem
    }
}
