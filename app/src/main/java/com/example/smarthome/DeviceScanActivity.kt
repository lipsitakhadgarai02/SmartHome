package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DeviceScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        setupNavigation()
        setupScanningSimulation()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupScanningSimulation() {
        // Find the "Scan Nearby Device" card by the ID we added to the XML
        val scanCard = findViewById<CardView>(R.id.cv_scan_nearby)

        scanCard?.setOnClickListener {
            // 1. Provide immediate visual feedback
            Toast.makeText(this, "Searching for nearby smart devices...", Toast.LENGTH_SHORT).show()

            // 2. Simulate background scanning delay (2 seconds)
            Handler(Looper.getMainLooper()).postDelayed({
                // 3. Navigate to Discovery screen after "scan" is complete
                val intent = Intent(this, DeviceDiscoveryActivity::class.java)
                startActivity(intent)
            }, 2000)
        }
    }
}