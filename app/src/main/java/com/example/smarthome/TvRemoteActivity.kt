package com.example.smarthome

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

/**
 * TvRemoteActivity provides a digital remote interface for Smart TV control.
 */
class TvRemoteActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private var volume: Int = 25 // Default volume
    private val deviceId = "TV_UNIT_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_remote)

        // Restore state from manager
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)
        volume = DeviceStateManager.getDeviceValue(deviceId, 25)

        setupUI()
        
        // Handle modern back press logic
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupUI() {
        // Back Button
        findViewById<ImageView>(R.id.iv_back_remote)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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

        // Volume Controls
        val tvVolLevel = findViewById<TextView>(R.id.tv_volume_level)
        val ivVolUp = findViewById<ImageView>(R.id.iv_vol_up)
        val ivVolDown = findViewById<ImageView>(R.id.iv_vol_down)

        tvVolLevel?.text = "Vol: $volume"

        ivVolUp?.setOnClickListener {
            if (isDeviceOn) {
                if (volume < 100) {
                    volume += 1
                    if (volume > 100) volume = 100
                    updateVolumeUI(tvVolLevel)
                }
            } else {
                showPowerOffToast()
            }
        }

        ivVolDown?.setOnClickListener {
            if (isDeviceOn) {
                if (volume > 0) {
                    volume -= 1
                    if (volume < 0) volume = 0
                    updateVolumeUI(tvVolLevel)
                }
            } else {
                showPowerOffToast()
            }
        }

        // D-Pad and other buttons (Feedback Only)
        setupFeedbackButtons()
        
        // Initial visual state
        updateVisualState(powerButton)
    }

    private fun updateVolumeUI(tv: TextView?) {
        tv?.text = "Vol: $volume"
        DeviceStateManager.setDeviceValue(deviceId, volume)
    }

    private fun showPowerOffToast() {
        Toast.makeText(this, "Turn ON the TV first", Toast.LENGTH_SHORT).show()
    }

    private fun setupFeedbackButtons() {
        val buttons = listOf(
            R.id.btn_ok, R.id.btn_up, R.id.btn_down, R.id.btn_left, R.id.btn_right,
            R.id.btn_remote_back, R.id.btn_remote_tv, R.id.btn_remote_play, R.id.btn_remote_mute
        )

        buttons.forEach { id ->
            findViewById<View>(id)?.setOnClickListener {
                if (isDeviceOn) {
                    val name = resources.getResourceEntryName(id).replace("btn_", "").replace("_", " ").capitalize()
                    Toast.makeText(this, "$name pressed", Toast.LENGTH_SHORT).show()
                } else {
                    showPowerOffToast()
                }
            }
        }
    }

    private fun updateVisualState(powerButton: ImageButton?) {
        val dpadContainer = findViewById<View>(R.id.dpad_container)
        val volumeContainer = findViewById<View>(R.id.ll_volume_container)
        val volText = findViewById<View>(R.id.tv_volume_level)
        
        if (isDeviceOn) {
            powerButton?.setColorFilter(resources.getColor(R.color.accent_green, theme))
            dpadContainer?.alpha = 1.0f
            volumeContainer?.alpha = 1.0f
            volText?.alpha = 1.0f
        } else {
            powerButton?.setColorFilter(resources.getColor(R.color.primary_teal, theme))
            dpadContainer?.alpha = 0.4f
            volumeContainer?.alpha = 0.4f
            volText?.alpha = 0.4f
        }
    }
}
