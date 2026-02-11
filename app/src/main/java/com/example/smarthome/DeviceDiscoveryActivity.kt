package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * DeviceDiscoveryActivity handles the UI for scanning and finding nearby smart devices.
 * This activity is independent and follows the project's clean code standards.
 */
class DeviceDiscoveryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)

        setupClickListeners()

        // Handle modern back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupClickListeners() {
        // Back Button Logic
        findViewById<ImageView>(R.id.btn_back_discovery)?.setOnClickListener {
            finish()
        }

        // Logic for interacting with found devices can be added here
    }
}