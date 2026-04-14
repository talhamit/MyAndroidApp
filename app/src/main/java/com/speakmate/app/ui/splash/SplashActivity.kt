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
 * Splash screen shown for 2.5 s while the app initialises.
 * Uses a slide-up + fade animation on the logo and tagline.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo and title
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        binding.ivLogo.startAnimation(slideUp)
        binding.tvAppName.startAnimation(slideUp)
        binding.tvTagline.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in).also {
                it.startOffset = 200
            }
        )

        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}
