package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * DeviceDiscoveryActivity acts as the CreateAutomationActivity.
 * Allows users to configure and save a new automation.
 */
class DeviceDiscoveryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)

        val createMode = intent.getStringExtra("CREATE_MODE")

        // Simulation of Save Automation Flow
        // Using the existing back button as a proxy for the 'Save/Done' action for this step
        findViewById<Button>(R.id.btn_back_discovery)?.setOnClickListener {
            if (createMode == "AUTOMATION") {
                val intent = Intent(this, DeviceManagementActivity::class.java).apply {
                    putExtra("IS_SAVED", true)
                    putExtra("FLOW_TYPE", "AUTOMATION")
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
            finish()
        }
    }
}