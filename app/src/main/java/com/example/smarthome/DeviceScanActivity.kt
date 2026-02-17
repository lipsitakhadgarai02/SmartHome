package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DeviceScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}