package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class TvConnectionMethodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_connection_method)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // QR Method
        findViewById<CardView>(R.id.cv_qr_method).setOnClickListener {
            // Check if QrScannerActivity exists before starting
            // For now, using a safe approach
            try {
                startActivity(Intent(this, Class.forName("com.example.smarthome.QrScannerActivity")))
            } catch (e: Exception) {
                // Fallback or Toast
            }
        }

        // PIN Method
        findViewById<CardView>(R.id.cv_pin_method).setOnClickListener {
            try {
                startActivity(Intent(this, Class.forName("com.example.smarthome.TvPinEntryActivity")))
            } catch (e: Exception) {
                // Fallback or Toast
            }
        }

        // Wi-Fi Discovery Method
        findViewById<CardView>(R.id.cv_wifi_method).setOnClickListener {
            val intent = Intent(this, DeviceDiscoveryActivity::class.java).apply {
                putExtra("SCAN_MODE", "Wi-Fi")
            }
            startActivity(intent)
        }
    }
}