package com.speakmate.app.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakmate.app.databinding.FragmentProgressBinding
import com.speakmate.app.viewmodel.ProgressViewModel
import com.speakmate.app.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Progress screen.
 * Shows streak, average accuracy, total sessions, and history list.
 */
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        observeViewModel()

        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Progress")
                .setMessage("Are you sure you want to delete all practice history? This cannot be undone.")
                .setPositiveButton("Reset") { _, _ -> viewModel.resetProgress() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.streakInfo.observe(viewLifecycleOwner) { info ->
            binding.tvStreak.text       = "🔥 ${info.currentStreak} day streak"
            binding.tvLongestStreak.text = "Best: ${info.longestStreak} days"
            if (info.lastPracticeDate > 0) {
                val fmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                binding.tvLastPractice.text = "Last practice: ${fmt.format(Date(info.lastPracticeDate))}"
            }
        }

        viewModel.averageAccuracy.observe(viewLifecycleOwner) { avg ->
            binding.tvAvgAccuracy.text = "${avg.toInt()}%"
            binding.progressAccuracy.progress = avg.toInt()
        }

        viewModel.totalSessions.observe(viewLifecycleOwner) { total ->
            binding.tvTotalSessions.text = "$total sessions"
        }

        viewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            historyAdapter.submitList(sessions)
            binding.tvEmptyHistory.visibility =
                if (sessions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
