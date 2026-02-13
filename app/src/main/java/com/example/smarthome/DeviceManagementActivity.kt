package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * DeviceManagementActivity handles device status and monitoring.
 * Updated to match HomeActivity's exact navigation bar behavior and position.
 */
class DeviceManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_management)

        setupNavigation()
        setupBottomNavigation()
        
        // Exact same spacing as Home Page for 3-button navigation
        applyWindowInsets()
    }

    private fun setupNavigation() {
        val roomsContainer = findViewById<LinearLayout>(R.id.ll_rooms)
        roomsContainer?.getChildAt(0)?.setOnClickListener {
            startActivity(Intent(this, DeviceDiscoveryActivity::class.java).apply {
                putExtra("CREATE_MODE", "AUTOMATION")
            })
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav?.selectedItemId = R.id.nav_placeholder // Using placeholder or devices context

        bottomNav?.let { ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets -> insets } }

        bottomNav?.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNav.selectedItemId) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                R.id.nav_rooms -> {
                    startActivity(Intent(this, RoomsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, DeviceScanActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, AccountManagementActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    })
                    true
                }
                else -> false
            }
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

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("IS_SAVED", false)) {
            Toast.makeText(this, "Automation Saved Successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}