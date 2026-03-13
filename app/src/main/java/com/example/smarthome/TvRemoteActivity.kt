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
 * Integrated with Persistent TCP Sockets for real-time Laptop control.
 */
class TvRemoteActivity : AppCompatActivity() {

    private var isDeviceOn: Boolean = false
    private var volume: Int = 25
    private lateinit var deviceId: String
    private lateinit var deviceName: String
    private lateinit var deviceIp: String
    private var socketHandler: SocketHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_remote)

        // Get data from Intent
        deviceId = intent.getStringExtra("DEVICE_ID") ?: "tv_living_room"
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Laptop"
        deviceIp = intent.getStringExtra("DEVICE_IP") ?: ""
        val connectionMode = intent.getStringExtra("CONNECTION_MODE") ?: ""

        if (connectionMode == "SOCKET" && deviceIp.isNotEmpty()) {
            setupSocketConnection()
        }

        // Restore state
        isDeviceOn = DeviceStateManager.getDeviceState(deviceId)
        volume = DeviceStateManager.getDeviceValue(deviceId, 25)

        setupUI()
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupSocketConnection() {
        socketHandler = SocketHandler { response ->
            // Handle JSON response from Laptop
            Toast.makeText(this, "Response: $response", Toast.LENGTH_SHORT).show()
        }
        socketHandler?.connect(deviceIp)
    }

    private fun setupUI() {
        val tvStatus = findViewById<TextView>(R.id.tv_room_name)
        tvStatus?.text = if (deviceIp.isNotEmpty()) "$deviceName ($deviceIp)" else deviceName
        
        findViewById<ImageView>(R.id.iv_back_remote)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val powerButton = findViewById<ImageButton>(R.id.iv_power)
        powerButton?.setOnClickListener {
            isDeviceOn = !isDeviceOn
            DeviceStateManager.setDeviceState(deviceId, isDeviceOn)
            
            if (isDeviceOn) {
                socketHandler?.sendCommand("WAKE") // Example
            } else {
                socketHandler?.sendCommand("SHUTDOWN")
            }
            
            val message = if (isDeviceOn) "$deviceName Active" else "$deviceName Shutdown Sent"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            updateVisualState(powerButton)
        }

        val tvVolLevel = findViewById<TextView>(R.id.tv_volume_level)
        val ivVolUp = findViewById<ImageView>(R.id.iv_vol_up)
        val ivVolDown = findViewById<ImageView>(R.id.iv_vol_down)

        tvVolLevel?.text = "Vol: $volume"

        ivVolUp?.setOnClickListener {
            if (isDeviceOn) {
                socketHandler?.sendCommand("VOLUME_UP")
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
                socketHandler?.sendCommand("VOLUME_DOWN")
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
        Toast.makeText(this, "Turn ON the $deviceName first", Toast.LENGTH_SHORT).show()
    }

    private fun setupFeedbackButtons() {
        val buttons = mapOf(
            R.id.btn_ok to "LOCK_SCREEN",
            R.id.btn_remote_play to "PLAY_PAUSE",
            R.id.btn_remote_mute to "MUTE"
        )

        buttons.forEach { (id, command) ->
            findViewById<View>(id)?.setOnClickListener {
                if (isDeviceOn) {
                    socketHandler?.sendCommand(command)
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

    override fun onDestroy() {
        super.onDestroy()
        socketHandler?.closeConnection()
    }
}