package com.speakmate.app.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.speakmate.app.R
import com.speakmate.app.databinding.ActivityMainBinding

/**
 * Single-activity host. All screens are Fragments navigated via Navigation Component.
 *
 * FIX #4: With NoActionBar theme we must call setSupportActionBar(binding.toolbar)
 * BEFORE setupActionBarWithNavController, and the toolbar must be in the layout.
 * This was already correct — keeping as-is but adding null-safety guard.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register the Toolbar as the support action bar
        setSupportActionBar(binding.toolbar)

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Only the Home screen has no back arrow
        val appBarConfig = AppBarConfiguration(setOf(R.id.homeFragment))
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}
