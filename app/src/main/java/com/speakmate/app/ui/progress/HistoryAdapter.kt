package com.speakmate.app.ui.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.speakmate.app.R
import com.speakmate.app.data.model.PracticeSession
import com.speakmate.app.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<PracticeSession, HistoryAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: PracticeSession) {
            binding.tvSentence.text  = session.sentence.take(60) + if (session.sentence.length > 60) "…" else ""
            binding.tvMode.text      = session.mode
            binding.tvDate.text      = dateFormat.format(Date(session.date))
            binding.tvScore.text     = "${session.accuracyScore.toInt()}%"

            val color = when {
                session.accuracyScore >= 80 -> R.color.correct_green
                session.accuracyScore >= 50 -> R.color.warning_amber
                else                        -> R.color.error_red
            }
            binding.tvScore.setTextColor(
                ContextCompat.getColor(binding.root.context, color)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<PracticeSession>() {
        override fun areItemsTheSame(a: PracticeSession, b: PracticeSession) = a.id == b.id
        override fun areContentsTheSame(a: PracticeSession, b: PracticeSession) = a == b
    }
}
