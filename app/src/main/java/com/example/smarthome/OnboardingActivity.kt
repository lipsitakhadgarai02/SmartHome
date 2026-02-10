package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val btnNext = findViewById<Button>(R.id.btn_next)
        btnNext.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}