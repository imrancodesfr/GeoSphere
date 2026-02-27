package com.example.geosphere.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geosphere.databinding.ItemReportedQuestionBinding
import com.example.geosphere.utils.QuestionReport

class ReportedQuestionsAdapter(
    private val onDismissClicked: (QuestionReport) -> Unit,
    private val onEditClicked: (QuestionReport) -> Unit
) : ListAdapter<QuestionReport, ReportedQuestionsAdapter.ReportViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportedQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(
        private val binding: ItemReportedQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: QuestionReport) {
            binding.tvQuestionText.text = report.questionText
            binding.tvReportReason.text = "Reason: ${report.reason}"
            binding.tvReportedBy.text = "Reported by UID: ${report.reportedByUserId}"

            binding.btnDismiss.setOnClickListener { onDismissClicked(report) }
            binding.btnEdit.setOnClickListener { onEditClicked(report) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<QuestionReport>() {
        override fun areItemsTheSame(oldItem: QuestionReport, newItem: QuestionReport) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: QuestionReport, newItem: QuestionReport) =
            oldItem == newItem
    }
}
