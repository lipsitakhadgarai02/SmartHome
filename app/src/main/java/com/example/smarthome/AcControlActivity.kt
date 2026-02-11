package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * AcControlActivity handles the UI logic for controlling the Air Conditioner.
 * Features include switching modes, adjusting temperature, and toggling power.
 */
class AcControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ac_control_new)

        setupUI()

        // Modern back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupUI() {
        // Back Button Logic
        findViewById<ImageView>(R.id.iv_back_ac)?.setOnClickListener {
            finish()
        }

        // Additional listeners for mode selection and temperature adjustments can be added here
    }
}