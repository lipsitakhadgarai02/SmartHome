package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class DeviceDiscoveryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_discovery)

        // Back button navigation
        findViewById<ImageView>(R.id.btn_back_discovery).setOnClickListener {
            onBackPressed()
        }

        // Device selection navigation
        
        // AC card -> AcControlActivity
        findViewById<LinearLayout>(R.id.ll_found_ac).setOnClickListener {
            startActivity(Intent(this, AcControlActivity::class.java))
        }

        // Smart Fan -> LampControlActivity (as a substitute if FanControlActivity is missing) 
        // Or connect to RoomDetails/Management if available
        findViewById<LinearLayout>(R.id.ll_found_fan).setOnClickListener {
            startActivity(Intent(this, DeviceManagementActivity::class.java))
        }

        // Smart TV -> TvRemoteActivity
        findViewById<LinearLayout>(R.id.ll_found_tv).setOnClickListener {
            startActivity(Intent(this, TvRemoteActivity::class.java))
        }
    }
}