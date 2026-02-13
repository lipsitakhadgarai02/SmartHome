package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * RoomsActivity handles the Room List.
 * Updated to match HomeActivity's navigation bar behavior and position.
 */
class RoomsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupNavigation()
        setupRoomClickListeners()
        setupBottomNavigation()
        
        // Exact same spacing as Home Page
        applyWindowInsets()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav?.selectedItemId = R.id.nav_rooms

        // Disable internal padding to match Home page look
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
                R.id.nav_rooms -> true
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

    private fun setupRoomClickListeners() {
        val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as? ViewGroup
        val scrollView = root?.children?.firstOrNull { it is androidx.core.widget.NestedScrollView } as? ViewGroup
        val gridLayout = scrollView?.children?.firstOrNull { it is GridLayout } as? GridLayout

        gridLayout?.let { grid ->
            grid.getChildAt(1)?.setOnClickListener { navigateToDevice("Lamp", LampControlActivity::class.java, "Bed Room") }
            grid.getChildAt(2)?.setOnClickListener { navigateToDevice("AC", AcControlActivity::class.java, "Kitchen") }
            grid.getChildAt(3)?.setOnClickListener { navigateToDevice("TV", TvRemoteActivity::class.java, "Living Room") }
        }
    }

    private fun navigateToDevice(type: String, target: Class<*>, roomName: String) {
        startActivity(Intent(this, target).apply {
            putExtra("DEVICE_TYPE", type)
            putExtra("ROOM_NAME", roomName)
        })
    }
}