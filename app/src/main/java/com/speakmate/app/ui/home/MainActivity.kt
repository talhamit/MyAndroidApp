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
 * Single-activity host.
 * All screens are Fragments navigated via the Navigation Component.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Top-level destinations (no back arrow on these)
        val appBarConfig = AppBarConfiguration(
            setOf(R.id.homeFragment)
        )
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}
