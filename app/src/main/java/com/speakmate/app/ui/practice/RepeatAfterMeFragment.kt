package com.speakmate.app.ui.practice

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.speakmate.app.data.repository.ContentRepository
import com.speakmate.app.databinding.FragmentRepeatBinding
import com.speakmate.app.utils.TextToSpeechHelper
import java.io.File

/**
 * "Repeat After Me" mode.
 * App plays a sentence → user records → user replays their recording.
 */
class RepeatAfterMeFragment : Fragment() {

    private var _binding: FragmentRepeatBinding? = null
    private val binding get() = _binding!!

    private lateinit var tts: TextToSpeechHelper

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordingFile: File? = null
    private var isRecording = false
    private var hasRecording = false

    private val sentences = ContentRepository.practiceSentences.shuffled()
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepeatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeechHelper(requireContext())
        updateSentence()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Play sentence via TTS
        binding.btnPlay.setOnClickListener {
            tts.speak(sentences[currentIndex].text)
        }

        // Record / Stop recording
        binding.btnRecord.setOnClickListener {
            if (isRecording) stopRecording() else startRecording()
        }

        // Replay user's recording
        binding.btnReplay.setOnClickListener {
            playRecording()
        }

        // Next sentence
        binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % sentences.size
            hasRecording = false
            updateSentence()
        }
    }

    private fun updateSentence() {
        binding.tvSentence.text  = sentences[currentIndex].text
        binding.tvProgress.text  = "${currentIndex + 1} / ${sentences.size}"
        binding.btnReplay.isEnabled = hasRecording
        binding.tvStatus.text       = "Press ▶ to hear the sentence, then 🎙 to record"
    }

    private fun startRecording() {
        try {
            recordingFile = File(requireContext().cacheDir, "repeat_recording.3gp")
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            binding.btnRecord.text    = "⏹ Stop Recording"
            binding.tvStatus.text     = "Recording… speak now!"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Recording error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply { stop(); release() }
            recorder = null
            isRecording  = false
            hasRecording = true
            binding.btnRecord.text        = "🎙 Record Again"
            binding.btnReplay.isEnabled   = true
            binding.tvStatus.text         = "Recording saved! Tap ▶ Play Back to hear yourself."
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Stop error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playRecording() {
        val file = recordingFile ?: return
        if (!file.exists()) return
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener { binding.tvStatus.text = "Playback complete." }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Playback error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recorder?.release(); recorder = null
        player?.release();   player   = null
        tts.destroy()
        _binding = null
    }
}
