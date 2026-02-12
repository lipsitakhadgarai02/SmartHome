package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val btnGetIn = findViewById<View>(R.id.fl_get_in)
        btnGetIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}
