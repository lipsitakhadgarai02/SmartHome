package com.example.smarthome

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView

/**
 * AcControlActivity handles the UI logic for controlling the Air Conditioner.
 * Features include switching modes, adjusting temperature, and toggling power.
 * Implements state persistence to maintain state across sessions.
 */
class AcControlActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private var temperature: Int = 24 // Default temperature
    private val deviceId = "AC_UNIT_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ac_control_new)

        // Restore state from manager
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)
        temperature = DeviceStateManager.getDeviceValue(deviceId, 24)

        setupUI()
        updateVisualState()

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
            onBackPressedDispatcher.onBackPressed()
        }

        // Power Toggle Logic
        val switchContainer = findViewById<LinearLayout>(R.id.ll_ac_switch)
        val powerSwitch = switchContainer?.getChildAt(0) as? SwitchCompat
        val statusText = switchContainer?.getChildAt(1) as? TextView

        // Sync switch state with persisted state
        powerSwitch?.isChecked = isDeviceOn
        statusText?.text = if (isDeviceOn) "ON" else "OFF"

        powerSwitch?.setOnCheckedChangeListener { _, isChecked ->
            isDeviceOn = isChecked
            DeviceStateManager.setDeviceState(deviceId, isDeviceOn)
            
            statusText?.text = if (isDeviceOn) "ON" else "OFF"
            
            val message = if (isDeviceOn) "AC Turned ON" else "AC Turned OFF"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            updateVisualState()
        }

        // Temperature Control
        val tvTemp = findViewById<TextView>(R.id.tv_temp)
        val cvTempUp = findViewById<CardView>(R.id.iv_temp_up)
        val cvTempDown = findViewById<CardView>(R.id.iv_temp_down)

        tvTemp.text = "${temperature}°C"

        cvTempUp.setOnClickListener {
            if (isDeviceOn) {
                temperature++
                tvTemp.text = "${temperature}°C"
                DeviceStateManager.setDeviceValue(deviceId, temperature)
            }
        }

        cvTempDown.setOnClickListener {
            if (isDeviceOn) {
                temperature--
                tvTemp.text = "${temperature}°C"
                DeviceStateManager.setDeviceValue(deviceId, temperature)
            }
        }
    }

    private fun updateVisualState() {
        val tempControl = findViewById<View>(R.id.fl_temp_control)
        val modesContainer = findViewById<View>(R.id.ll_modes)
        
        val alphaValue = if (isDeviceOn) 1.0f else 0.4f
        tempControl?.alpha = alphaValue
        modesContainer?.alpha = alphaValue
    }
}