package com.speakmate.app.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.speakmate.app.R
import com.speakmate.app.databinding.FragmentGameBinding
import com.speakmate.app.viewmodel.GameViewModel
import com.speakmate.app.viewmodel.ViewModelFactory

/**
 * Sentence Builder Game.
 * Shuffled word chips → tap to build the correct sentence.
 */
class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GameViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.shuffledWords.observe(viewLifecycleOwner) { words ->
            buildWordBank(words)
        }

        viewModel.selectedWords.observe(viewLifecycleOwner) { words ->
            buildAnswerArea(words)
        }

        viewModel.isCorrect.observe(viewLifecycleOwner) { isCorrect ->
            when (isCorrect) {
                true -> {
                    binding.tvResult.text  = "✅ Correct! Well done!"
                    binding.tvResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.correct_green))
                    binding.tvResult.visibility   = View.VISIBLE
                    binding.btnCheck.visibility   = View.GONE
                    binding.btnNext.visibility    = View.VISIBLE
                }
                false -> {
                    val correct = viewModel.correctSentence.value ?: ""
                    binding.tvResult.text = "❌ Not quite.\n\nCorrect: \"$correct\""
                    binding.tvResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                    binding.tvResult.visibility   = View.VISIBLE
                    binding.btnCheck.visibility   = View.GONE
                    binding.btnNext.visibility    = View.VISIBLE
                }
                null -> {
                    binding.tvResult.visibility  = View.GONE
                    binding.btnCheck.visibility  = View.VISIBLE
                    binding.btnNext.visibility   = View.GONE
                }
            }
        }

        viewModel.score.observe(viewLifecycleOwner) { score ->
            binding.tvScore.text = "Score: $score"
        }

        viewModel.round.observe(viewLifecycleOwner) { round ->
            binding.tvRound.text = "Round $round"
        }
    }

    private fun setupClickListeners() {
        binding.btnCheck.setOnClickListener {
            if ((viewModel.selectedWords.value?.size ?: 0) == 0) return@setOnClickListener
            viewModel.checkAnswer()
        }
        binding.btnNext.setOnClickListener { viewModel.loadNextSentence() }
    }

    /** Build tappable word chips in the word bank (shuffled pool). */
    private fun buildWordBank(words: List<String>) {
        binding.chipGroupBank.removeAllViews()
        words.forEach { word ->
            val chip = makeChip(word)
            chip.setOnClickListener { viewModel.selectWord(word) }
            binding.chipGroupBank.addView(chip)
        }
    }

    /** Build selected words in the answer area. */
    private fun buildAnswerArea(words: List<String>) {
        binding.chipGroupAnswer.removeAllViews()
        if (words.isEmpty()) {
            val hint = TextView(requireContext()).apply {
                text = "Tap words above to build the sentence"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
            }
            binding.chipGroupAnswer.addView(hint)
            return
        }
        words.forEach { word ->
            val chip = makeChip(word, selected = true)
            chip.setOnClickListener { viewModel.deselectWord(word) }
            binding.chipGroupAnswer.addView(chip)
        }
    }

    private fun makeChip(word: String, selected: Boolean = false): Chip {
        return Chip(requireContext()).apply {
            text = word
            isClickable = true
            isCheckable = false
            chipBackgroundColor = if (selected)
                ContextCompat.getColorStateList(requireContext(), R.color.primary_blue_light)
            else
                ContextCompat.getColorStateList(requireContext(), R.color.chip_background)
            setTextColor(ContextCompat.getColor(requireContext(),
                if (selected) R.color.white else R.color.text_primary))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
