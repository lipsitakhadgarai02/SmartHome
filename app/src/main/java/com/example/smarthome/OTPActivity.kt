package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OTPActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        findViewById<Button>(R.id.btn_verify).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity() // Clear task stack
        }
    }
}