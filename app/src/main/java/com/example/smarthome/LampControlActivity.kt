package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * LampControlActivity handles the UI logic for controlling a Smart Lamp.
 * It includes features like toggling power, adjusting brightness, and viewing usage stats.
 */
class LampControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lamp_control)

        setupClickListeners()

        // Implementing modern back press dispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupClickListeners() {
        // Back Button
        findViewById<ImageView>(R.id.iv_back_lamp)?.setOnClickListener {
            finish()
        }

        // Add additional logic for Switch, SeekBar and dynamic UI updates here
    }
}