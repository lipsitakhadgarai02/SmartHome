package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val ivMenu = findViewById<ImageView>(R.id.iv_menu)

        // Hamburger Click - Open Drawer
        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Drawer Navigation Items Click
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_home -> {
                    // Already on Home
                }
                R.id.drawer_devices -> {
                    startActivity(Intent(this, DeviceScanActivity::class.java))
                }
                R.id.drawer_add_device -> {
                    startActivity(Intent(this, DeviceDiscoveryActivity::class.java))
                }
                R.id.drawer_automations -> {
                    // TODO: Connect Automations Screen
                }
                R.id.drawer_notifications -> {
                    // TODO: Connect Notifications Screen
                }
                R.id.drawer_settings -> {
                    // TODO: Connect Settings Screen
                }
                R.id.drawer_logout -> {
                    startActivity(Intent(this, SignInActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Bottom Navigation Logic (Preserved)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_rooms -> {
                    startActivity(Intent(this, RoomsActivity::class.java))
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, DeviceScanActivity::class.java))
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}