package com.example.smarthome

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fan_control)

        intent.getStringExtra("DEVICE_ID")?.let { deviceId = it }
        
        setupUI()
        setupRealTimeMetrics()
        handleBackNavigation()
    }

    private fun setupUI() {
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val ivFan = findViewById<ImageView>(R.id.iv_fan_image)
        val cvPower = findViewById<CardView>(R.id.cv_power_button)
        val ivPowerIcon = findViewById<ImageView>(R.id.iv_power_icon)
        val sliderSpeed = findViewById<Slider>(R.id.slider_fan_speed)

        isPoweredOn = DeviceStateManager.getDeviceState(deviceId)
        val currentSpeed = DeviceStateManager.getDeviceValue(deviceId, 1)

        updatePowerUI(cvPower, ivPowerIcon, ivFan)
        sliderSpeed.value = currentSpeed.toFloat()

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

    private fun setupRealTimeMetrics() {
        val tvRpm = findViewById<TextView>(R.id.tv_fan_rpm)
        val tvUsage = findViewById<TextView>(R.id.tv_fan_usage)

        updateRunnable = object : Runnable {
            override fun run() {
                if (isPoweredOn) {
                    tvRpm.text = "${DeviceStateManager.getSimulatedRPM(deviceId)} RPM"
                    tvUsage.text = DeviceStateManager.getEnergyUsage(deviceId)
                } else {
                    tvRpm.text = "0 RPM"
                    tvUsage.text = DeviceStateManager.getEnergyUsage(deviceId)
                }
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(updateRunnable)
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}