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
 *
 * FIX #2 (Kotlin side): removed binding.btnRecord.text / binding.btnReplay.isEnabled
 * calls that assumed FAB — those properties don't exist on FAB.
 * The layout now uses MaterialButton for all three buttons, so .text works fine.
 *
 * FIX #3: MediaRecorder() no-arg constructor is deprecated in API 31+ and
 * removed in some vendor builds. Always use MediaRecorder(context) on S+.
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
        updateSentenceUI()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPlay.setOnClickListener {
            tts.speak(sentences[currentIndex].text)
        }

        binding.btnRecord.setOnClickListener {
            if (isRecording) stopRecording() else startRecording()
        }

        binding.btnReplay.setOnClickListener {
            playRecording()
        }

        binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % sentences.size
            hasRecording = false
            updateSentenceUI()
        }
    }

    private fun updateSentenceUI() {
        binding.tvSentence.text = sentences[currentIndex].text
        binding.tvProgress.text = "${currentIndex + 1} / ${sentences.size}"
        // MaterialButton supports isEnabled and text
        binding.btnReplay.isEnabled = hasRecording
        binding.btnRecord.text = "🎙 Record"
        binding.tvStatus.text = "Press PLAY to hear the sentence, then RECORD to practice"
    }

    private fun startRecording() {
        if (!hasAudioPermission()) {
            Toast.makeText(requireContext(), "Microphone permission needed", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            recordingFile = File(requireContext().cacheDir, "repeat_recording.3gp")

            // FIX #3: always use context-aware constructor; wrap deprecated call properly
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder!!.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            binding.btnRecord.text = "⏹ Stop"
            binding.tvStatus.text = "Recording… speak now!"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Recording error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply { stop(); release() }
            recorder = null
            isRecording = false
            hasRecording = true
            binding.btnRecord.text = "🎙 Record Again"
            binding.btnReplay.isEnabled = true
            binding.tvStatus.text = "Done! Tap ↩ Replay to hear yourself."
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
                setOnCompletionListener {
                    if (_binding != null) binding.tvStatus.text = "Playback complete."
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Playback error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasAudioPermission(): Boolean {
        return requireContext().checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recorder?.release(); recorder = null
        player?.release(); player = null
        tts.destroy()
        _binding = null
    }
}
