package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView

class RoomDetailsActivity : AppCompatActivity() {

    private var currentRoomName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_details)

        currentRoomName = intent.getStringExtra("ROOM_NAME") ?: "Room Details"
        findViewById<TextView>(R.id.tv_room_name).text = currentRoomName

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        setupDeviceList(currentRoomName)
    }

    private fun setupDeviceList(roomName: String) {
        val glDevices = findViewById<GridLayout>(R.id.gl_devices)
        glDevices.removeAllViews()
        
        val devices = DeviceStateManager.getDevicesInRoom(roomName)
        val inflater = LayoutInflater.from(this)

        if (devices.isEmpty() && (roomName == "Bed Room" || roomName == "Kitchen" || roomName == "Living Room")) {
            // Handle static rooms for legacy support if needed
            if (roomName == "Bed Room") addDeviceCard(glDevices, inflater, "Lamp", "bedroom_lamp_1", LampControlActivity::class.java)
            if (roomName == "Kitchen") addDeviceCard(glDevices, inflater, "AC", "kitchen_ac_1", AcControlActivity::class.java)
            if (roomName == "Living Room") addDeviceCard(glDevices, inflater, "TV", "living_tv_1", TvRemoteActivity::class.java)
        } else {
            devices.forEach { deviceType ->
                val deviceId = "${roomName.replace(" ", "_").lowercase()}_${deviceType.lowercase()}"
                when (deviceType) {
                    "AC" -> addDeviceCard(glDevices, inflater, "Air Conditioner", deviceId, AcControlActivity::class.java)
                    "Light" -> addDeviceCard(glDevices, inflater, "Smart Light", deviceId, LampControlActivity::class.java)
                    "Fan" -> addDeviceCard(glDevices, inflater, "Smart Fan", deviceId, FanControlActivity::class.java)
                    "Speaker" -> addDeviceCard(glDevices, inflater, "Smart Speaker", deviceId, SpeakerControlActivity::class.java)
                }
            }
        }

        // Add "Add Device" card at the end
        addPlusCard(glDevices, inflater)
    }

    private fun addDeviceCard(glDevices: GridLayout, inflater: LayoutInflater, name: String, deviceId: String, target: Class<*>) {
        val card = inflater.inflate(R.layout.layout_device_item_small, glDevices, false) as CardView
        card.findViewById<TextView>(R.id.tv_device_name).text = name
        
        val icon = card.findViewById<ImageView>(R.id.iv_device_icon)
        val isPaired = DeviceStateManager.isDevicePaired(deviceId)
        
        card.alpha = if (isPaired) 1.0f else 0.5f
        
        when (name) {
            "Air Conditioner" -> icon.setImageResource(R.drawable.ic_ac)
            "Smart Light" -> icon.setImageResource(R.drawable.ic_bulb)
            "Smart Fan" -> {
                icon.setImageResource(R.drawable.ic_launcher_foreground)
                icon.setColorFilter(getColor(R.color.primary_teal))
            }
            "Lamp" -> icon.setImageResource(R.drawable.ic_lamp)
            "TV" -> icon.setImageResource(R.drawable.ic_tv)
            "Smart Speaker" -> icon.setImageResource(R.drawable.ic_speaker)
        }

        card.setOnClickListener {
            if (DeviceStateManager.isDevicePaired(deviceId)) {
                startActivity(Intent(this, target).apply {
                    putExtra("DEVICE_ID", deviceId)
                    putExtra("DEVICE_NAME", name)
                })
            } else {
                showPairingDialog(name, deviceId)
            }
        }

        card.setOnLongClickListener {
            if (DeviceStateManager.isDevicePaired(deviceId)) {
                showUnpairingDialog(name, deviceId)
            }
            true
        }

        setGridLayoutParams(card)
        glDevices.addView(card)
    }

    private fun addPlusCard(glDevices: GridLayout, inflater: LayoutInflater) {
        val card = inflater.inflate(R.layout.layout_device_item_small, glDevices, false) as CardView
        card.findViewById<TextView>(R.id.tv_device_name).text = "Add Device"
        card.findViewById<ImageView>(R.id.iv_device_icon).setImageResource(R.drawable.ic_plus)
        card.findViewById<ImageView>(R.id.iv_device_icon).setColorFilter(getColor(R.color.primary_teal))
        
        card.setOnClickListener {
            showAddDeviceDialog()
        }

        setGridLayoutParams(card)
        glDevices.addView(card)
    }

    private fun setGridLayoutParams(card: View) {
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = GridLayout.LayoutParams.WRAP_CONTENT
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        params.setMargins(16, 16, 16, 16)
        card.layoutParams = params
    }

    private fun showAddDeviceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<TextView>(android.R.id.title)?.text = "Add Devices to Room"
        val etRoomName = dialogView.findViewById<EditText>(R.id.et_room_name)
        etRoomName.setText(currentRoomName)
        etRoomName.isEnabled = false // User can't change room name here

        val switchAc = dialogView.findViewById<SwitchCompat>(R.id.switch_ac)
        val switchLight = dialogView.findViewById<SwitchCompat>(R.id.switch_light)
        val switchFan = dialogView.findViewById<SwitchCompat>(R.id.switch_fan)
        val btnCreate = dialogView.findViewById<Button>(R.id.btn_create)
        btnCreate.text = "Update Room"

        // Pre-fill existing devices
        val existing = DeviceStateManager.getDevicesInRoom(currentRoomName)
        if (existing.contains("AC")) switchAc.isChecked = true
        if (existing.contains("Light")) switchLight.isChecked = true
        if (existing.contains("Fan")) switchFan.isChecked = true

        btnCreate.setOnClickListener {
            val devices = mutableListOf<String>()
            if (switchAc.isChecked) devices.add("AC")
            if (switchLight.isChecked) devices.add("Light")
            if (switchFan.isChecked) devices.add("Fan")
            // Note: Speaker can be added here if you update the dialog XML, 
            // but for now we follow your existing dialog structure.

            DeviceStateManager.addRoom(currentRoomName, devices)
            Toast.makeText(this, "Devices updated!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            setupDeviceList(currentRoomName)
        }
        dialog.show()
    }

    private fun showPairingDialog(deviceName: String, deviceId: String) {
        AlertDialog.Builder(this)
            .setTitle("Pair Device")
            .setMessage("Would you like to pair with $deviceName?")
            .setPositiveButton("Pair") { _, _ ->
                DeviceStateManager.pairDevice(deviceId)
                Toast.makeText(this, "$deviceName paired successfully!", Toast.LENGTH_SHORT).show()
                setupDeviceList(currentRoomName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUnpairingDialog(deviceName: String, deviceId: String) {
        AlertDialog.Builder(this)
            .setTitle("Unpair Device")
            .setMessage("Are you sure you want to unpair $deviceName?")
            .setPositiveButton("Unpair") { _, _ ->
                DeviceStateManager.unpairDevice(deviceId)
                Toast.makeText(this, "$deviceName unpaired.", Toast.LENGTH_SHORT).show()
                setupDeviceList(currentRoomName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}