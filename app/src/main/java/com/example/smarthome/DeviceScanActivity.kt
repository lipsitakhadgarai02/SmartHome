package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView

class DeviceScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        setupBottomNavigation()
        setupClickListeners()
        
        // Fix for bottom navigation clipping and positioning
        applyWindowInsets()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav?.let {
            it.selectedItemId = R.id.nav_devices
            
            // Disable internal inset handling to prevent icon clipping
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets -> insets }

            it.setOnItemSelectedListener { item ->
                if (item.itemId == it.selectedItemId) return@setOnItemSelectedListener true
                
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        })
                        true
                    }
                    R.id.nav_devices -> true
                    R.id.nav_rooms -> {
                        // Assuming RoomsActivity exists as per HomeActivity navigation
                        try {
                            startActivity(Intent(this, Class.forName("com.example.smarthome.RoomsActivity")).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            })
                        } catch (e: Exception) {}
                        true
                    }
                    R.id.nav_settings -> {
                        startActivity(Intent(this, ProfileActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        })
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            finish()
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