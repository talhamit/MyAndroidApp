package com.speakmate.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.speakmate.app.R
import com.speakmate.app.databinding.ActivitySplashBinding
import com.speakmate.app.ui.home.MainActivity

/**
 * Splash screen shown for 2 seconds.
 *
 * FIX #5: Wrapped animation loading in try/catch so a missing anim resource
 * won't crash the app. Also cancelled the handler callback in onDestroy to
 * prevent leaks when the user backs out quickly.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private val navigateRunnable = Runnable {
        if (!isFinishing) {
            startActivity(Intent(this, MainActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate — safe even if animation resource is missing
        try {
            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            binding.ivLogo.startAnimation(slideUp)
            binding.tvAppName.startAnimation(slideUp)
            binding.tvTagline.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).also {
                    it.startOffset = 200
                }
            )
        } catch (e: Exception) {
            // Animation not critical — continue without it
        }

        handler.postDelayed(navigateRunnable, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(navigateRunnable)
    }
}
