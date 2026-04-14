package com.speakmate.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.speakmate.app.R
import com.speakmate.app.databinding.FragmentHomeBinding

/**
 * Home screen – grid of feature cards.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animate cards in with staggered delay
        val cards = listOf(
            binding.cardPractice, binding.cardConversation, binding.cardGame,
            binding.cardRepeat, binding.cardAI, binding.cardProgress
        )
        cards.forEachIndexed { i, card ->
            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade_in)
            anim.startOffset = (i * 80).toLong()
            card.startAnimation(anim)
        }

        // Navigation
        binding.cardPractice.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_practice)
        }
        binding.cardConversation.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_conversation)
        }
        binding.cardGame.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_game)
        }
        binding.cardRepeat.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_repeat)
        }
        binding.cardAI.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_ai)
        }
        binding.cardProgress.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_progress)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
