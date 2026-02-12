package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_rooms -> {
                    startActivity(Intent(this, RoomsActivity::class.java))
                    true
                }
                R.id.nav_devices -> {
                    startActivity(Intent(this, DeviceScanActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    // Start Settings activity if it exists
                    true
                }
                else -> false
            }
        }
    }
}