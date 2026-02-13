package com.example.smarthome

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * TvRemoteActivity provides a digital remote interface for Smart TV control.
 * This activity is independent and follows the project's clean architecture.
 * Implements state persistence to maintain state across sessions.
 */
class TvRemoteActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private val deviceId = "TV_UNIT_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_remote)

        // Restore state from manager
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)

        setupUI()
        
        // Handle modern back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupUI() {
        // Back Button Logic
        findViewById<ImageView>(R.id.iv_back_remote)?.setOnClickListener {
            finish()
        }

        // Power Toggle Logic
        val powerButton = findViewById<ImageButton>(R.id.iv_power)
        powerButton?.setOnClickListener {
            isDeviceOn = !isDeviceOn
            DeviceStateManager.setDeviceState(deviceId, isDeviceOn)
            
            val message = if (isDeviceOn) "TV Turned ON" else "TV Turned OFF"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            updateVisualState(powerButton)
        }
        
        // Initial visual state
        updateVisualState(powerButton)
    }

    private fun updateVisualState(powerButton: ImageButton?) {
        val dpadContainer = findViewById<android.view.View>(R.id.dpad_container)
        
        if (isDeviceOn) {
            powerButton?.setColorFilter(resources.getColor(R.color.accent_green, theme))
            dpadContainer?.alpha = 1.0f
        } else {
            powerButton?.setColorFilter(resources.getColor(R.color.primary_teal, theme))
            dpadContainer?.alpha = 0.4f
        }
    }
}