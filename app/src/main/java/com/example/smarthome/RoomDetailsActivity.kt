package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class RoomDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_details)

        val roomName = intent.getStringExtra("ROOM_NAME") ?: "Room Details"
        findViewById<TextView>(R.id.tv_room_name).text = roomName

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        setupDeviceList(roomName)
    }

    private fun setupDeviceList(roomName: String) {
        val glDevices = findViewById<GridLayout>(R.id.gl_devices)
        val devices = DeviceStateManager.getDevicesInRoom(roomName)
        val inflater = LayoutInflater.from(this)

        if (devices.isEmpty()) {
            // Handle case where room has no devices (static rooms if needed)
            if (roomName == "Bed Room") addDeviceCard(glDevices, inflater, "Lamp", LampControlActivity::class.java)
            if (roomName == "Kitchen") addDeviceCard(glDevices, inflater, "AC", AcControlActivity::class.java)
            if (roomName == "Living Room") addDeviceCard(glDevices, inflater, "TV", TvRemoteActivity::class.java)
        } else {
            devices.forEach { deviceType ->
                when (deviceType) {
                    "AC" -> addDeviceCard(glDevices, inflater, "Air Conditioner", AcControlActivity::class.java)
                    "Light" -> addDeviceCard(glDevices, inflater, "Smart Light", LampControlActivity::class.java)
                    "Fan" -> addDeviceCard(glDevices, inflater, "Smart Fan", FanControlActivity::class.java)
                }
            }
        }
    }

    private fun addDeviceCard(glDevices: GridLayout, inflater: LayoutInflater, name: String, target: Class<*>) {
        val card = inflater.inflate(R.layout.layout_device_item_small, glDevices, false) as CardView
        card.findViewById<TextView>(R.id.tv_device_name).text = name
        
        // Set appropriate icon based on name
        val icon = card.findViewById<ImageView>(R.id.iv_device_icon)
        when (name) {
            "Air Conditioner" -> icon.setImageResource(R.drawable.ic_ac)
            "Smart Light" -> icon.setImageResource(R.drawable.ic_bulb)
            "Smart Fan" -> {
                icon.setImageResource(R.drawable.ic_launcher_foreground)
                icon.setColorFilter(getColor(R.color.primary_teal))
            }
            "Lamp" -> icon.setImageResource(R.drawable.ic_lamp)
            "TV" -> icon.setImageResource(R.drawable.ic_tv)
        }

        card.setOnClickListener {
            startActivity(Intent(this, target).apply {
                putExtra("DEVICE_NAME", name)
            })
        }

        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = GridLayout.LayoutParams.WRAP_CONTENT
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        params.setMargins(16, 16, 16, 16)
        card.layoutParams = params

        glDevices.addView(card)
    }
}