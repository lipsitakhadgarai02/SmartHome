package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI and Navigation
        setupDrawer()
        setupBottomNavigation()
        setupClickListeners()
        handleBackNavigation()
        
        // Fix for 3-button navigation spacing and icon clipping
        applyWindowInsets()
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val ivMenu = findViewById<ImageView>(R.id.iv_menu)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_devices -> {
                    val intent = Intent(this, DeviceScanActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                }
                R.id.drawer_add_device -> {
                    val intent = Intent(this, DeviceScanActivity::class.java).apply {
                        putExtra("SCAN_MODE", "QR_CODE")
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                }
                R.id.drawer_automations -> {
                    val intent = Intent(this, DeviceManagementActivity::class.java).apply {
                        putExtra("FLOW_TYPE", "AUTOMATION")
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    }
                    startActivity(intent)
                }
                R.id.drawer_notifications -> navigateToNotifications()
                R.id.drawer_settings -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                }
                R.id.drawer_logout -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home

        // Disable internal inset handling to prevent icon clipping
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { _, insets -> insets }

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNav.selectedItemId) return@setOnItemSelectedListener true

            when (item.itemId) {
                R.id.nav_home -> true
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
                    navigateToNotifications()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.cv_energy)?.setOnClickListener {
            startActivity(Intent(this, DeviceManagementActivity::class.java).apply {
                putExtra("FLOW_TYPE", "SENSOR_MONITORING")
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }

        findViewById<Button>(R.id.btn_go_devices)?.setOnClickListener {
            startActivity(Intent(this, DeviceScanActivity::class.java).apply {
                putExtra("SCAN_MODE", "MANUAL")
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
        }

        findViewById<ImageView>(R.id.iv_notification).setOnClickListener {
            navigateToNotifications()
        }
    }

    private fun applyWindowInsets() {
        val bottomNavContainer = findViewById<View>(R.id.cv_bottom_nav)
        val micButton = findViewById<View>(R.id.cv_mic)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            bottomNavContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomInsets(systemBars.bottom)
            }
            
            micButton?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomInsets(systemBars.bottom)
            }
            
            insets
        }
    }

    private fun ViewGroup.MarginLayoutParams.bottomInsets(insetBottom: Int) {
        val density = resources.displayMetrics.density
        val baseMargin = (24 * density).toInt()
        bottomMargin = baseMargin + insetBottom
    }

    private fun handleBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    showExitDialog()
                }
            }
        })
    }

    private fun navigateToNotifications() {
        startActivity(Intent(this, AccountManagementActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
    }

    private fun performLogout() {
        getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}