package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/**
 * AcControlActivity handles the UI logic for controlling the Air Conditioner.
 * Features include switching modes, adjusting temperature, and toggling power.
 * Implements state persistence to maintain state across sessions.
 */
class AcControlActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private val deviceId = "AC_UNIT_1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ac_control_new)

        // Restore state from manager
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)

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
            finish()
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
    }

    private fun updateVisualState() {
        val tempControl = findViewById<android.view.View>(R.id.fl_temp_control)
        val modesContainer = findViewById<android.view.View>(R.id.ll_modes)
        
        val alphaValue = if (isDeviceOn) 1.0f else 0.4f
        tempControl?.alpha = alphaValue
        modesContainer?.alpha = alphaValue
    }
}