package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TvPinEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_pin_entry)

        val etPin = findViewById<EditText>(R.id.et_pin)
        val btnVerify = findViewById<Button>(R.id.btn_verify)
        val progressBar = findViewById<ProgressBar>(R.id.pb_loading)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnVerify.setOnClickListener {
            val pin = etPin.text.toString().trim()
            if (pin.length == 4) {
                // Simulation of verification
                btnVerify.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                
                Handler(Looper.getMainLooper()).postDelayed({
                    if (pin == "1234") {
                        connectSuccess()
                    } else {
                        btnVerify.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Invalid PIN. Try 1234 for demo", Toast.LENGTH_SHORT).show()
                    }
                }, 2000)
            } else {
                Toast.makeText(this, "Please enter 4-digit PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectSuccess() {
        val deviceId = "tv_living_room"
        DeviceStateManager.pairDevice(deviceId)
        
        Toast.makeText(this, "TV Paired Successfully!", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, TvRemoteActivity::class.java).apply {
            putExtra("DEVICE_ID", deviceId)
            putExtra("CONNECTION_METHOD", "PIN")
        }
        startActivity(intent)
        finish()
    }
}