package com.example.smarthome

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider

class FanControlActivity : AppCompatActivity() {

    private var deviceId = "fan_unit_1"
    private var isPoweredOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fan_control)

        // Get device ID from intent if available
        intent.getStringExtra("DEVICE_ID")?.let { deviceId = it }
        
        setupUI()
        handleBackNavigation()
    }

    private fun setupUI() {
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val tvTitle = findViewById<TextView>(R.id.tv_fan_title)
        val ivFan = findViewById<ImageView>(R.id.iv_fan_image)
        val cvPower = findViewById<CardView>(R.id.cv_power_button)
        val ivPowerIcon = findViewById<ImageView>(R.id.iv_power_icon)
        val sliderSpeed = findViewById<Slider>(R.id.slider_fan_speed)

        // Initialize state from Manager
        isPoweredOn = DeviceStateManager.getDeviceState(deviceId)
        val currentSpeed = DeviceStateManager.getDeviceValue(deviceId, 1)

        updatePowerUI(cvPower, ivPowerIcon, ivFan)
        sliderSpeed.value = currentSpeed.toFloat()

        // Listeners
        btnBack.setOnClickListener { finish() }

        cvPower.setOnClickListener {
            isPoweredOn = !isPoweredOn
            DeviceStateManager.setDeviceState(deviceId, isPoweredOn)
            updatePowerUI(cvPower, ivPowerIcon, ivFan)
        }

        sliderSpeed.addOnChangeListener { _, value, _ ->
            DeviceStateManager.setDeviceValue(deviceId, value.toInt())
        }
    }

    private fun updatePowerUI(cvPower: CardView, ivPowerIcon: ImageView, ivFan: ImageView) {
        if (isPoweredOn) {
            cvPower.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_teal))
            ivPowerIcon.setColorFilter(ContextCompat.getColor(this, R.color.white))
            ivFan.alpha = 1.0f
        } else {
            cvPower.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            ivPowerIcon.setColorFilter(ContextCompat.getColor(this, R.color.white))
            ivFan.alpha = 0.3f
        }
    }

    private fun handleBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}