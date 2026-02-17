package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/**
 * LampControlActivity handles the UI logic for controlling a Smart Lamp.
 * It includes features like toggling power, adjusting brightness, and viewing usage stats.
 * Implements state persistence to maintain state across sessions.
 */
class LampControlActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private var intensity: Int = 80 // Default intensity
    private val deviceId = "LAMP_UNIT_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lamp_control)

        // Restore state from manager
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)
        intensity = DeviceStateManager.getDeviceValue(deviceId, 80)

        setupUI()
        updateVisualState()

        // Implementing modern back press dispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupUI() {
        // Back Button
        findViewById<ImageView>(R.id.iv_back_lamp)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Power Toggle Logic
        val switchContainer = findViewById<LinearLayout>(R.id.ll_switch)
        val powerSwitch = switchContainer?.getChildAt(0) as? SwitchCompat
        val statusText = switchContainer?.getChildAt(1) as? TextView

        // Sync switch state with persisted state
        powerSwitch?.isChecked = isDeviceOn
        statusText?.text = if (isDeviceOn) "ON" else "OFF"

        powerSwitch?.setOnCheckedChangeListener { _, isChecked ->
            isDeviceOn = isChecked
            DeviceStateManager.setDeviceState(deviceId, isDeviceOn)
            
            statusText?.text = if (isDeviceOn) "ON" else "OFF"
            
            val message = if (isDeviceOn) "Lamp Turned ON" else "Lamp Turned OFF"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            updateVisualState()
        }

        // Intensity Control
        val tvBrightness = findViewById<TextView>(R.id.tv_brightness_percent)
        val sbIntensity = findViewById<SeekBar>(R.id.sb_intensity)
        val ivUp = findViewById<ImageView>(R.id.iv_intensity_up)
        val ivDown = findViewById<ImageView>(R.id.iv_intensity_down)

        tvBrightness.text = "$intensity%"
        sbIntensity.progress = intensity

        sbIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isDeviceOn) {
                    intensity = progress
                    tvBrightness.text = "$intensity%"
                    DeviceStateManager.setDeviceValue(deviceId, intensity)
                } else if (fromUser) {
                    seekBar?.progress = intensity // Snap back if OFF
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        ivUp.setOnClickListener {
            if (isDeviceOn && intensity < 100) {
                intensity += 5
                if (intensity > 100) intensity = 100
                updateIntensityUI(tvBrightness, sbIntensity)
            }
        }

        ivDown.setOnClickListener {
            if (isDeviceOn && intensity > 0) {
                intensity -= 5
                if (intensity < 0) intensity = 0
                updateIntensityUI(tvBrightness, sbIntensity)
            }
        }
    }

    private fun updateIntensityUI(tv: TextView, sb: SeekBar) {
        tv.text = "$intensity%"
        sb.progress = intensity
        DeviceStateManager.setDeviceValue(deviceId, intensity)
    }

    private fun updateVisualState() {
        val lampVisual = findViewById<android.view.View>(R.id.fl_lamp_visual)
        val statsCard = findViewById<android.view.View>(R.id.ll_stats_card)
        val brightnessText = findViewById<android.view.View>(R.id.tv_brightness_percent)
        val sliderContainer = findViewById<android.view.View>(R.id.sb_intensity)?.parent as? android.view.View
        
        val alphaValue = if (isDeviceOn) 1.0f else 0.4f
        lampVisual?.alpha = alphaValue
        statsCard?.alpha = alphaValue
        brightnessText?.alpha = alphaValue
        sliderContainer?.alpha = alphaValue
    }
}