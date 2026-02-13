package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * ProfileActivity handles the Search categories.
 * Updated to match HomeActivity's exact navigation bar behavior and position.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupClickListeners()
        setupBottomNavigation()
        
        // Match Home Page spacing
        applyWindowInsets()
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.iv_back_search)?.setOnClickListener { finish() }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        
        // Disable internal padding to match Home page
        bottomNav?.let { ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets -> insets } }

        val cvBottomNav = findViewById<View>(R.id.cv_bottom_nav)
        val homeItem = cvBottomNav?.findViewById<View>(R.id.ic_home) // Accessing by layout structure if IDs are limited
        
        // Since activity_search uses an include, we handle clicks on the bottom bar components
        findViewById<View>(R.id.cv_mic)?.setOnClickListener {
            // Voice command simulation
        }
    }

    private fun applyWindowInsets() {
        val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
        val micButton = findViewById<View>(R.id.cv_mic)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            bottomNavContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val density = resources.displayMetrics.density
                bottomMargin = (24 * density).toInt() + systemBars.bottom
            }
            
            micButton?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val density = resources.displayMetrics.density
                bottomMargin = (32 * density).toInt() + systemBars.bottom
            }
            insets
        }
    }
}