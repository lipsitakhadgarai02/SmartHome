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
 * TvRemoteActivity provides a digital remote interface for Smart TV control via Wi-Fi.
 */
class TvRemoteActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private var volume: Int = 25
    private var deviceId = "tv_living_room" // Unified device ID from Discovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_remote)

        // Restore state from manager or Firebase
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)
        volume = DeviceStateManager.getDeviceValue(deviceId, 25)

        setupUI()
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupUI() {
        // Status Header
        val tvStatus = findViewById<TextView>(R.id.tv_room_name)
        tvStatus?.text = "Living Room TV (Wi-Fi)"
        
        findViewById<ImageView>(R.id.iv_back_remote)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val powerButton = findViewById<ImageButton>(R.id.iv_power)
        powerButton?.setOnClickListener {
            isDeviceOn = !isDeviceOn
            DeviceStateManager.setDeviceState(deviceId, isDeviceOn)
            
            val message = if (isDeviceOn) "TV Turned ON" else "TV Turned OFF"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            updateVisualState(powerButton)
        }

        val tvVolLevel = findViewById<TextView>(R.id.tv_volume_level)
        val ivVolUp = findViewById<ImageView>(R.id.iv_vol_up)
        val ivVolDown = findViewById<ImageView>(R.id.iv_vol_down)

        tvVolLevel?.text = "Vol: $volume"

        ivVolUp?.setOnClickListener {
            if (isDeviceOn) {
                if (volume < 100) {
                    volume += 1
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
                    updateVolumeUI(tvVolLevel)
                }
            } else {
                showPowerOffToast()
            }
        }

        setupFeedbackButtons()
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
                    Toast.makeText(this, "$name pressed (via Wi-Fi)", Toast.LENGTH_SHORT).show()
                } else {
                    showPowerOffToast()
                }
            }
        }
    }

    private fun updateVisualState(powerButton: ImageButton?) {
        val dpadContainer = findViewById<View>(R.id.dpad_container)
        val volumeContainer = findViewById<View>(R.id.ll_volume_container)
        
        if (isDeviceOn) {
            powerButton?.setColorFilter(resources.getColor(R.color.accent_green, theme))
            dpadContainer?.alpha = 1.0f
            volumeContainer?.alpha = 1.0f
        } else {
            powerButton?.setColorFilter(resources.getColor(R.color.primary_teal, theme))
            dpadContainer?.alpha = 0.4f
            volumeContainer?.alpha = 0.4f
        }
    }
}