package com.speakmate.app.ui.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakmate.app.databinding.FragmentAiConversationBinding
import com.speakmate.app.utils.PermissionHelper
import com.speakmate.app.utils.PrefsManager
import com.speakmate.app.utils.SpeechRecognizerHelper
import com.speakmate.app.utils.TextToSpeechHelper
import com.speakmate.app.viewmodel.AIConversationViewModel
import com.speakmate.app.viewmodel.ViewModelFactory

/**
 * AI Conversation Mode.
 * User speaks → text sent to OpenAI → response read aloud via TTS.
 * Requires an API key set in Settings.
 */
class AIConversationFragment : Fragment() {

    private var _binding: FragmentAiConversationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AIConversationViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    private lateinit var tts: TextToSpeechHelper
    private var speechRecognizer: SpeechRecognizerHelper? = null
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var prefs: PrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PrefsManager(requireContext())
        viewModel.initialise(prefs.openAiApiKey)

        // TTS
        tts = TextToSpeechHelper(requireContext())

        // Speech recognizer
        if (SpeechRecognizerHelper.isAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizerHelper(
                context       = requireContext(),
                onResult      = { text ->
                    binding.etMessage.setText(text)
                    viewModel.sendMessage(text)
                },
                onError       = { msg -> showToast(msg) },
                onStateChange = { listening -> viewModel.onListeningStateChanged(listening) }
            )
        }

        // RecyclerView
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).also { it.stackFromEnd = true }
            adapter = chatAdapter
        }

        observeViewModel()
        setupClickListeners()

        // Show API key warning if not configured
        if (prefs.openAiApiKey.isBlank()) {
            binding.cardApiWarning.visibility = View.VISIBLE
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled      = !loading
            binding.btnMic.isEnabled       = !loading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showToast(it) }
        }

        viewModel.latestReply.observe(viewLifecycleOwner) { reply ->
            reply?.let {
                tts.speak(it)
                viewModel.clearLatestReply()
            }
        }

        viewModel.isListening.observe(viewLifecycleOwner) { listening ->
            binding.btnMic.isSelected = listening
        }
    }

    private fun setupClickListeners() {
        // Send typed message
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text?.toString()?.trim() ?: return@setOnClickListener
            if (text.isBlank()) return@setOnClickListener
            viewModel.sendMessage(text)
            binding.etMessage.setText("")
        }

        // Voice input
        binding.btnMic.setOnClickListener {
            if (!PermissionHelper.hasAudioPermission(requireContext())) {
                PermissionHelper.requestAudioPermission(requireActivity())
                return@setOnClickListener
            }
            val sr = speechRecognizer ?: return@setOnClickListener
            if (sr.isListening()) sr.stopListening() else sr.startListening()
        }

        // Clear chat
        binding.btnClear.setOnClickListener { viewModel.clearChat() }
    }

    private fun showToast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        tts.destroy()
        speechRecognizer?.destroy()
        _binding = null
    }
}
