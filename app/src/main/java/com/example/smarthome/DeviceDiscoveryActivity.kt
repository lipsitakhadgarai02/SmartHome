package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DeviceDiscoveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)

        // Back button navigation
        findViewById<ImageView>(R.id.btn_back_discovery).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup fake pairing logic for each found device
        setupPairingListeners()
    }

    private fun setupPairingListeners() {
        // AC card -> AcControlActivity
        findViewById<LinearLayout>(R.id.ll_found_ac).setOnClickListener {
            pairAndNavigate("ac_living_room", AcControlActivity::class.java, "Air Conditioner")
        }

        // Smart Fan -> LampControlActivity (as a substitute)
        findViewById<LinearLayout>(R.id.ll_found_fan).setOnClickListener {
            pairAndNavigate("lamp_bedroom", LampControlActivity::class.java, "Smart Lamp")
        }

        // Smart TV -> TvRemoteActivity
        findViewById<LinearLayout>(R.id.ll_found_tv).setOnClickListener {
            pairAndNavigate("tv_living_room", TvRemoteActivity::class.java, "Smart TV")
        }
    }

    private fun pairAndNavigate(deviceId: String, targetActivity: Class<*>, deviceName: String) {
        // 1. Save the paired device to the session manager
        DeviceStateManager.pairDevice(deviceId)

        // 2. Give user feedback
        Toast.makeText(this, "$deviceName Paired Successfully", Toast.LENGTH_SHORT).show()

        // 3. Navigate to the device's control screen
        val intent = Intent(this, targetActivity)
        startActivity(intent)

        // Finish this screen to clean up the back stack
        finish()
    }
}