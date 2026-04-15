package com.speakmate.app.ui.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.speakmate.app.databinding.FragmentConversationBinding
import com.speakmate.app.utils.PermissionHelper
import com.speakmate.app.utils.SpeechRecognizerHelper
import com.speakmate.app.utils.TextToSpeechHelper
import com.speakmate.app.viewmodel.ConversationViewModel
import com.speakmate.app.viewmodel.ViewModelFactory

/**
 * Daily Conversation Mode.
 * FIX #9: TTS / speech callbacks guarded against detached fragment.
 */
class ConversationFragment : Fragment() {

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConversationViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    private var tts: TextToSpeechHelper? = null
    private var speechRecognizer: SpeechRecognizerHelper? = null
    private var currentCategory = "restaurant"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeechHelper(requireContext())

        if (SpeechRecognizerHelper.isAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizerHelper(
                context       = requireContext(),
                onResult      = { text -> view.post { if (_binding != null) viewModel.onUserReply(text) } },
                onError       = { msg  -> view.post { if (_binding != null) showToast(msg) } },
                onStateChange = { listening -> view.post { if (_binding != null) viewModel.onListeningStateChanged(listening) } }
            )
        }

        setupCategoryChips()
        observeViewModel()
        setupClickListeners()
        viewModel.loadCategory(currentCategory)
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            "restaurant"    to "🍽 Restaurant",
            "job_interview" to "💼 Interview",
            "daily_talk"    to "💬 Daily Talk"
        )
        categories.forEach { (id, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = (id == currentCategory)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        currentCategory = id
                        viewModel.loadCategory(id)
                    }
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun observeViewModel() {
        viewModel.currentTurnIndex.observe(viewLifecycleOwner) { updateTurnUI() }

        viewModel.userReply.observe(viewLifecycleOwner) { reply ->
            if (reply.isNotBlank()) {
                binding.tvUserReply.text       = "You said: \"$reply\""
                binding.tvUserReply.visibility = View.VISIBLE
            } else {
                binding.tvUserReply.visibility = View.GONE
            }
        }

        viewModel.feedback.observe(viewLifecycleOwner) { feedback ->
            if (feedback.isNotBlank()) {
                binding.tvFeedback.text          = feedback
                binding.cardFeedback.visibility  = View.VISIBLE
                binding.btnNext.visibility       = View.VISIBLE
            } else {
                binding.cardFeedback.visibility  = View.GONE
                binding.btnNext.visibility       = View.GONE
            }
        }

        viewModel.score.observe(viewLifecycleOwner) { score ->
            binding.tvScore.text       = if (score > 0f) "${score.toInt()}%" else ""
            binding.tvScore.visibility = if (score > 0f) View.VISIBLE else View.GONE
        }

        viewModel.isListening.observe(viewLifecycleOwner) { listening ->
            binding.btnMic.isSelected  = listening
            binding.tvMicStatus.text   = if (listening) "Listening…" else "Tap to answer"
        }
    }

    private fun setupClickListeners() {
        binding.btnPlayQuestion.setOnClickListener {
            val q = viewModel.currentTurn()?.question ?: return@setOnClickListener
            tts?.speak(q)
        }

        binding.btnMic.setOnClickListener {
            if (!PermissionHelper.hasAudioPermission(requireContext())) {
                PermissionHelper.requestAudioPermission(requireActivity())
                return@setOnClickListener
            }
            val sr = speechRecognizer ?: return@setOnClickListener
            if (sr.isListening()) sr.stopListening() else sr.startListening()
        }

        binding.btnNext.setOnClickListener {
            if (viewModel.isLastTurn()) {
                Toast.makeText(requireContext(), "Conversation complete! Great job! 🎉", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                viewModel.nextTurn()
            }
        }
    }

    private fun updateTurnUI() {
        val turn  = viewModel.currentTurn() ?: return
        val turns = viewModel.turns.value   ?: return
        val index = viewModel.currentTurnIndex.value ?: 0

        binding.tvQuestion.text          = turn.question
        binding.tvProgress.text          = "Turn ${index + 1} of ${turns.size}"
        binding.cardFeedback.visibility  = View.GONE
        binding.tvUserReply.visibility   = View.GONE
        binding.tvScore.visibility       = View.GONE
        binding.btnNext.visibility       = View.GONE

        // Auto-play the question
        tts?.speak(turn.question)
    }

    private fun showToast(msg: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.destroy()
        speechRecognizer?.destroy()
        _binding = null
    }
}
