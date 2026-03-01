package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DeviceScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        setupNavigation()
        setupScanningActions()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupScanningActions() {
        // Main Scan Card
        findViewById<CardView>(R.id.cv_scan_nearby)?.setOnClickListener {
            startActivity(Intent(this, TvConnectionMethodActivity::class.java))
        }

        // Category Grid Listeners
        findViewById<CardView>(R.id.cv_cat_tv)?.setOnClickListener {
            startActivity(Intent(this, TvConnectionMethodActivity::class.java))
        }

        findViewById<CardView>(R.id.cv_cat_wifi)?.setOnClickListener {
            val intent = Intent(this, DeviceDiscoveryActivity::class.java).apply {
                putExtra("SCAN_MODE", "Wi-Fi")
            }
            startActivity(intent)
        }

        // Placeholder for other categories
        val otherCatIds = intArrayOf(
            R.id.cv_cat_ac, R.id.cv_cat_bulb, R.id.cv_cat_fan,
            R.id.cv_cat_speaker, R.id.cv_cat_lamp, R.id.cv_cat_washing_machine,
            R.id.cv_cat_dvd
        )

        for (id in otherCatIds) {
            findViewById<CardView>(id)?.setOnClickListener {
                Toast.makeText(this, "Scanning for this category...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DeviceDiscoveryActivity::class.java)
                startActivity(intent)
            }
        }
    }
}