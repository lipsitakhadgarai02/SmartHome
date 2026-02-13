package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * DeviceManagementActivity handles device status and monitoring.
 */
class DeviceManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_management)

        setupNavigation()
    }

    private fun setupNavigation() {
        val roomsContainer = findViewById<LinearLayout>(R.id.ll_rooms)
        roomsContainer?.getChildAt(0)?.setOnClickListener {
            startActivity(Intent(this, DeviceDiscoveryActivity::class.java).apply {
                putExtra("CREATE_MODE", "AUTOMATION")
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("IS_SAVED", false)) {
            Toast.makeText(this, "Automation Saved Successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}