package com.speakmate.app.ui.practice

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.speakmate.app.R
import com.speakmate.app.data.model.WordStatus
import com.speakmate.app.databinding.FragmentPracticeBinding
import com.speakmate.app.utils.PermissionHelper
import com.speakmate.app.utils.SpeechRecognizerHelper
import com.speakmate.app.utils.TextToSpeechHelper
import com.speakmate.app.utils.TextComparisonUtil
import com.speakmate.app.viewmodel.PracticeViewModel
import com.speakmate.app.viewmodel.ViewModelFactory

/**
 * Speaking Practice screen.
 * FIX #8: TTS callbacks now post to main thread before touching views,
 * preventing crashes when callbacks fire after fragment detaches.
 */
class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PracticeViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    private var tts: TextToSpeechHelper? = null
    private var speechRecognizer: SpeechRecognizerHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TTS — guard binding access with _binding != null check
        tts = TextToSpeechHelper(
            requireContext(),
            onDone  = { view.post { if (_binding != null) binding.btnListen.isEnabled = true } },
            onStart = { view.post { if (_binding != null) binding.btnListen.isEnabled = false } }
        )

        // Speech Recognizer
        if (SpeechRecognizerHelper.isAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizerHelper(
                context         = requireContext(),
                onResult        = { text -> view.post { if (_binding != null) viewModel.onSpeechResult(text) } },
                onPartialResult = { partial -> view.post { if (_binding != null) viewModel.onPartialResult(partial) } },
                onError         = { msg -> view.post { if (_binding != null) showToast(msg) } },
                onStateChange   = { listening -> view.post { if (_binding != null) viewModel.onListeningStateChanged(listening) } }
            )
        } else {
            showToast("Speech recognition not available on this device")
        }

        viewModel.loadSentences()
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.currentIndex.observe(viewLifecycleOwner) { updateSentenceUI() }

        viewModel.recognizedText.observe(viewLifecycleOwner) { text ->
            binding.tvRecognizedText.text = text
        }

        viewModel.comparisonResult.observe(viewLifecycleOwner) { result ->
            if (result == null) {
                binding.tvRecognizedText.text = ""
                binding.tvScore.visibility    = View.GONE
                binding.tvGrade.visibility    = View.GONE
                binding.cardResult.visibility = View.GONE
                return@observe
            }

            binding.tvScore.text       = "${result.accuracyScore.toInt()}%"
            binding.tvGrade.text       = TextComparisonUtil.gradeLabel(result.accuracyScore)
            binding.tvScore.visibility = View.VISIBLE
            binding.tvGrade.visibility = View.VISIBLE
            binding.cardResult.visibility = View.VISIBLE

            val colour = when {
                result.accuracyScore >= 80 -> ContextCompat.getColor(requireContext(), R.color.correct_green)
                result.accuracyScore >= 50 -> ContextCompat.getColor(requireContext(), R.color.warning_amber)
                else                       -> ContextCompat.getColor(requireContext(), R.color.error_red)
            }
            binding.tvScore.setTextColor(colour)

            // Word-level highlighting
            val ssb = SpannableStringBuilder()
            result.expectedWords.forEachIndexed { i, wordResult ->
                val spanColour = when (wordResult.status) {
                    WordStatus.CORRECT -> ContextCompat.getColor(requireContext(), R.color.correct_green)
                    WordStatus.WRONG   -> ContextCompat.getColor(requireContext(), R.color.error_red)
                    WordStatus.MISSING -> ContextCompat.getColor(requireContext(), R.color.warning_amber)
                }
                val start = ssb.length
                ssb.append(wordResult.word)
                ssb.setSpan(ForegroundColorSpan(spanColour), start, ssb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (wordResult.status != WordStatus.CORRECT) {
                    ssb.setSpan(BackgroundColorSpan(Color.parseColor("#1A000000")), start, ssb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (i < result.expectedWords.size - 1) ssb.append(" ")
            }
            binding.tvHighlightedResult.text = ssb
        }

        viewModel.isListening.observe(viewLifecycleOwner) { listening ->
            binding.btnMic.isSelected   = listening
            binding.tvMicHint.text      = if (listening) "Listening…" else "Tap mic to speak"
            binding.micPulse.visibility = if (listening) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnListen.setOnClickListener {
            val sentence = viewModel.currentSentence()?.text ?: return@setOnClickListener
            tts?.speak(sentence)
        }

        binding.btnMic.setOnClickListener {
            if (!PermissionHelper.hasAudioPermission(requireContext())) {
                PermissionHelper.requestAudioPermission(requireActivity())
                return@setOnClickListener
            }
            val sr = speechRecognizer ?: return@setOnClickListener
            if (sr.isListening()) sr.stopListening() else sr.startListening()
        }

        binding.btnNext.setOnClickListener     { viewModel.nextSentence() }
        binding.btnPrevious.setOnClickListener { viewModel.previousSentence() }
    }

    private fun updateSentenceUI() {
        val sentence = viewModel.currentSentence() ?: return
        val list     = viewModel.sentences.value ?: return
        val index    = viewModel.currentIndex.value ?: 0

        binding.tvSentence.text  = sentence.text
        binding.tvCategory.text  = sentence.category.replaceFirstChar { it.uppercase() }
        binding.tvProgress.text  = "${index + 1} / ${list.size}"
        binding.tvTip.text       = sentence.tip
        binding.tvTip.visibility = if (sentence.tip.isNotEmpty()) View.VISIBLE else View.GONE
        binding.cardResult.visibility = View.GONE
        binding.tvScore.visibility    = View.GONE
        binding.tvGrade.visibility    = View.GONE
        binding.tvRecognizedText.text = ""
    }

    private fun showToast(message: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.destroy()
        speechRecognizer?.destroy()
        _binding = null
    }
}
