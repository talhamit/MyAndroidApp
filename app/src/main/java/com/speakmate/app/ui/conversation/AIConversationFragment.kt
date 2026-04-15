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
 * FIX #10: Guards all callbacks and view accesses with _binding != null.
 */
class AIConversationFragment : Fragment() {

    private var _binding: FragmentAiConversationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AIConversationViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    private var tts: TextToSpeechHelper? = null
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

        tts = TextToSpeechHelper(requireContext())

        if (SpeechRecognizerHelper.isAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizerHelper(
                context       = requireContext(),
                onResult      = { text ->
                    view.post {
                        if (_binding != null) {
                            binding.etMessage.setText(text)
                            viewModel.sendMessage(text)
                        }
                    }
                },
                onError       = { msg -> view.post { if (_binding != null) showToast(msg) } },
                onStateChange = { listening -> view.post { if (_binding != null) viewModel.onListeningStateChanged(listening) } }
            )
        }

        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).also { it.stackFromEnd = true }
            adapter = chatAdapter
        }

        observeViewModel()
        setupClickListeners()

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
                tts?.speak(it)
                viewModel.clearLatestReply()
            }
        }

        viewModel.isListening.observe(viewLifecycleOwner) { listening ->
            binding.btnMic.isSelected = listening
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text?.toString()?.trim() ?: return@setOnClickListener
            if (text.isBlank()) return@setOnClickListener
            viewModel.sendMessage(text)
            binding.etMessage.setText("")
        }

        binding.btnMic.setOnClickListener {
            if (!PermissionHelper.hasAudioPermission(requireContext())) {
                PermissionHelper.requestAudioPermission(requireActivity())
                return@setOnClickListener
            }
            val sr = speechRecognizer ?: return@setOnClickListener
            if (sr.isListening()) sr.stopListening() else sr.startListening()
        }

        binding.btnClear.setOnClickListener { viewModel.clearChat() }
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
