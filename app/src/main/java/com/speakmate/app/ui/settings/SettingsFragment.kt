package com.speakmate.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.speakmate.app.databinding.FragmentSettingsBinding
import com.speakmate.app.utils.PrefsManager

/**
 * Settings screen.
 * Users can enter their OpenAI API key, adjust speech rate, etc.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PrefsManager(requireContext())
        loadSettings()
        setupSaveButton()
    }

    private fun loadSettings() {
        binding.etApiKey.setText(prefs.openAiApiKey)
        binding.sliderSpeechRate.value = prefs.speechRate
        binding.switchShowTips.isChecked = prefs.showTips
        binding.sliderDailyGoal.value = prefs.dailyGoalMinutes.toFloat()
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            prefs.openAiApiKey     = binding.etApiKey.text?.toString()?.trim() ?: ""
            prefs.speechRate       = binding.sliderSpeechRate.value
            prefs.showTips         = binding.switchShowTips.isChecked
            prefs.dailyGoalMinutes = binding.sliderDailyGoal.value.toInt()

            Toast.makeText(requireContext(), "Settings saved ✓", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
